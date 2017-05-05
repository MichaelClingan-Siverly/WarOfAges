<?php
        //the necessary parts to connect to a mysql database using php
        $host = "mysql.cs.iastate.edu";
        $db_user = "dbu309yt05";
        $db_password = "hatEG8rt8km";
        $db_name = "db309yt05";
        $port=3306;

        $con = mysqli_connect($host,$db_user,$db_password,$db_name,$port) or die('Unable to Connect');
?>
