<?php
require "dbConnect.php";
$data = file_get_contents('php://input');
$decoded = json_decode($data, true);

$userID = $decoded[0]["userID"];
$GridID = $decoded[1]["GridID"];
$UnitID = $decoded[2]["UnitID"];
$health = $decoded[3]["health"];

$response = array();

$sql = "insert into UnitMap value (".$GridID.",".$UnitID.", ".$health.",'".$userID."')";
echo($sql);
if(mysqli_query($con,$sql)){
	$code = "creation_success";
	$message = "'".$userID."' created a unit";
	array_push($response, array("code"=>$code));
	array_push($response, array("message"=>$message));
}
else{
	$code = "creation_failed";
	$message = "An error has occurred";
	array_push($response, array("code"=>$code));
	array_push($response, array("message"=>$message));
}
echo json_encode($response);

mysqli_close($con);
?>