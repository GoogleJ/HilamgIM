# HilamgIMService

#### Introduce
Dev repository of HilamgIMService，release on maven（XXXXXX）

#### How to use
1.Add "implementation project(':HilamgKit')" in your app's build.gradle file  
2.Use

    String action = "default";
    String url = "hilamg://imservice/?action=" + action;
    Intent i = new Intent(Intent.ACTION_VIEW);
    i.setData(Uri.parse(url));
    startActivity(i);

