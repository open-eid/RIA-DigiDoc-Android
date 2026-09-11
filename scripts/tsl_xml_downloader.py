#!/usr/bin/env python3

import argparse
import urllib.request
from pathlib import Path
from xml.dom import minidom

TSL_MIME_TYPE = 'application/vnd.etsi.tsl+xml'
DOWNLOAD_TIMEOUT_SECONDS = 60
DOWNLOAD_DIRECTORY = Path(__file__).resolve().parent / 'TSL'


def parse_arguments():
    """Parse command-line arguments."""
    parser = argparse.ArgumentParser(description='TSL XML Downloader')
    parser.add_argument('--tslFile', action='append', default=[], type=Path, metavar='PATH',
                        help='Base file (LOTL) to look members up in. Repeatable, searched in order')
    parser.add_argument('--members', required=True, type=str,
                        help='Scheme territories to download, e.g. "EE;EE_T;SE_TESTING"')
    return parser.parse_args()


def child_elements(parent, local_name):
    """Direct child elements with this local name, whatever XML namespace prefix the file uses."""
    return [
        node
        for node in parent.childNodes
        if node.nodeType == node.ELEMENT_NODE and node.localName == local_name
    ]


def text_of(element):
    """Text content of an element, or None when it is empty."""
    return element.firstChild.nodeValue if element.firstChild is not None else None


def other_information(pointer, local_name):
    """Values of AdditionalInformation/OtherInformation/<local_name> for one pointer."""
    return [
        text_of(element)
        for additional_information in child_elements(pointer, 'AdditionalInformation')
        for information in child_elements(additional_information, 'OtherInformation')
        for element in child_elements(information, local_name)
    ]


def find_tsl_url(member, lotl):
    """The TSL location the base file points at for this member, or None."""
    for pointers in lotl.getElementsByTagName('PointersToOtherTSL'):
        for pointer in child_elements(pointers, 'OtherTSLPointer'):
            if member not in other_information(pointer, 'SchemeTerritory'):
                continue
            if TSL_MIME_TYPE not in other_information(pointer, 'MimeType'):
                continue

            locations = child_elements(pointer, 'TSLLocation')

            if locations:
                return text_of(locations[0])

    return None


def existing_base_files(paths):
    """The given base files that exist, in the order given, reporting the ones that do not."""
    existing = []

    for path in paths:
        if path.is_file():
            existing.append(path)
        else:
            print(f"Skipping missing base file '{path}'")

    return existing


def resolve_member(member, base_files):
    """Look the member up in each base file in turn. Returns the first TSL location found."""
    for base_file in base_files:
        tsl_url = find_tsl_url(member, minidom.parse(str(base_file)))

        if tsl_url is not None:
            print(f"Found '{member}' in '{base_file}'")
            return tsl_url

        print(f"No '{member}' pointer in '{base_file}'")

    return None


def download_tsl(tsl_url, destination_path):
    """Download a TSL and save it."""
    print(f"Downloading {tsl_url} to {destination_path}")

    with urllib.request.urlopen(tsl_url, timeout=DOWNLOAD_TIMEOUT_SECONDS) as response:
        destination_path.write_bytes(response.read())


def main():
    """Main function to execute the TSL downloader."""
    args = parse_arguments()
    members = [member for member in args.members.split(';') if member]
    base_files = existing_base_files(args.tslFile)

    DOWNLOAD_DIRECTORY.mkdir(parents=True, exist_ok=True)

    for member in members:
        tsl_url = resolve_member(member, base_files)

        if tsl_url is None:
            searched = ', '.join(str(base_file) for base_file in base_files) or 'none'
            raise SystemExit(
                f"Could not find a TSL pointer for '{member}'. Base files searched: {searched}. "
                f"Either the member code is wrong, or the base file listing it was not given."
            )

        download_tsl(tsl_url, DOWNLOAD_DIRECTORY / f"{member}.xml")


if __name__ == "__main__":
    main()
