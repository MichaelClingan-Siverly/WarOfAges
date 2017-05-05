<?php
require 'dbConnect.php';

$data = file_get_contents('php://input');
$decoded = json_decode($data, true);

$userID = $decoded[0]["userID"];
$newLocal = $decoded[1]["newID"];
$oldLocal = $decoded[2]["oldID"];

$response = array();
//select the owner based on the old location of the unit
$sql = "select userID from UnitMap where GridID = ".$oldLocal.";";
$result = mysqli_query($con,$sql);

// If the new location is the same spot as the old location, send back that no changes occurred
if($newLocal == $oldLocal){
	$code = "no changes";	
	array_push($response, array("code"=>$code));
	echo json_encode($response);
}
else{
	//updating to new location based on the unit's owner and old location
	$sql = "update UnitMap set GridID = ".$newLocal." where GridID =".$oldLocal." AND userID = '".$userID."'";
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
