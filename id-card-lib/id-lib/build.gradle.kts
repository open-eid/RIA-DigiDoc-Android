import com.android.build.api.attributes.BuildTypeAttr

listOf("debug", "release").forEach { buildType ->
    configurations.maybeCreate(buildType).attributes {
        attribute(BuildTypeAttr.ATTRIBUTE, objects.named<BuildTypeAttr>(buildType))
    }
    artifacts.add(buildType, file("id-card-lib-$buildType.aar"))
}
