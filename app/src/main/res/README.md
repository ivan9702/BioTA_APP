請將
value
values-sw360dp-land
values-sw600dp-land
values-sw700dp-land
values-sw800dp-land

改成
value
values-sw360dp
values-sw600dp
values-sw700dp
values-sw800dp

否則
在 LoginActivity 按下 『HOME』 button，會導致
convertView = inflater.inflate(R.layout.list_item_login, parent, false);
發生例外

Android 如何選擇資源
https://developer.android.com/guide/topics/resources/providing-resources.html#BestMatch

http://www.tivix.com/blog/perfect-resource-image-size-dpi-for-any-android-de/