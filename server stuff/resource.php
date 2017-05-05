<?php
require "dbConnect.php";
$data = file_get_contents('php://input');
$decoded = json_decode($data, true);

$userID = $decoded[0]["userID"];
$amount = $decoded[1]["ResourceAmount"];
$resource = 0;

$sql = "Select M.AreaType, U.ResourceAmount from Map M, users U where M.AreaType = town and U.userID = '".$userID."';";
$result = mysqli_query($con,$sql);
if(mysqli_num_rows($result)>0){
	$row = mysqli_fetch_row($result);
	$terrain = $row[0];
	$resource = $row[1];
	if(strcmp($terrain,"town")==0){
		$resource += amount; 
		$sql = "update users set ResourceAmount = ".$resource." where userID = '".$userID."';";
		if(mysqli_query($con,$sql)){
			$code = "addition_success";
			$message = "'".$userID."' gained ".$amount." resources";
			array_push($response, array("code"=>$code));
			array_push($response, array("message"=>$message));
		}
		else{
			$code = "additon_failed";
			$message = "An error has occurred";
			array_push($response, array("code"=>$code));
			array_push($response, array("message"=>$message));
		}
	}
}

mysqli_close($con);
?>