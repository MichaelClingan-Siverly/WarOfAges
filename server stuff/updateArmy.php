<?php
require 'dbConnect.php';

$data = file_get_contents('php://input');
$decoded = json_decode($data, true);

$userID = $decoded[0]["userID"];
$GridID = $decoded[1]["mapID"];
$oldLocal = $decoded[2]["OldLocation"];

$response = array();

$sql = "select userID from UnitMap where GridID = ".$oldLocal.";";
$result = mysqli_query($con,$sql);

if($newlocal == $oldLocal){
	$code = "no changes";	
	array_push($response, array("code"=>$code));
	echo json_encode($response);
}
else{
	//updating the location based on the players's input
	$sql = "update Army set GridID = ".$newlocal." where userID ='".$userID."'";
	$response = array();
	if(mysqli_query($con,$sql)){
		
		$code = "update_success";
		$message = "The UnitMap is updated";
		array_push($response,array("code"=>$code));
		array_push($response,array("message"=>$message));
		//The array is turned in a JSON object and outputted
		echo json_encode($response);
	}
	else{
		$code = "update_failed";
		$message = "The UnitMap was not updated";
		array_push($response,array("code"=>$code));
		array_push($response,array("message"=>$message));
		//The code and message is turned in a JSON object and outputted
		echo json_encode($response);
	}
}
mysqli_close($con);
?>
