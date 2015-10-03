#Image library for android.

##Views
###AspectRatioImageView
To use this view, add following to your layout insted of a normal ImageView:
``` xml
<com.rkitmedia.rkimage.AspectRatioImageView
  android:layout_width="match_parent"
  android:layout_height="match_parent"
  app:aspectRatio="1.2"
  app:aspectRatioEnabled="true"
  app:dominantMeasurement="width" />
```
Following options are available:
* dominantMeasurement
   * ```width``` if height should depend on width
   * ```height``` if width should depend on height
* aspectRatio
   * the desired aspectRatio in width/height as float value
* aspectRatioEnabled
  * ```true``` if aspectRatio should be used for view measurement or ```false``` if not to
