<?php
//connect to sql database
require "dbConnect.php";

//var_dump($_POST);
$data = file_get_contents('php://input');
//$data = json_decode($data);

echo $data;

mysqli_close($con);
?>
