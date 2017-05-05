<?php
//connect to sql database
require "dbConnect.php";

$data = file_get_contents('php://input');
$decoded = json_decode($data, true);

$userID = $decoded[0]["userID"];
$nickname = $decoded[1]["nickname"];

//update nickname in users based on userID 
$sql = "Update users set nickname = '".$nickname."' where userID = '".$userID."';";

$result = mysqli_query($con,$sql);
$response = array();

if($result){
	// if the userID and nickname are not empty
	if($userID != "" && $nickname != ""){
		$code = "mod_succeed";
		$message = "nickname changed";
		array_push($response, array("code"=>$code);
		array_push($response, array("nickname"=>$nickname));
		array_push($response, array("message"=>$message));
		//The array is turned in a JSON object and outputted
		echo json_encode($response);
	}
	else{
		$code = "mod_failed";
		$message = "There is no input";
		array_push($response, array("code"=>$code));
		array_push($response, array("message"=>$message));
		//The array is turned in a JSON object and outputted
		echo json_encode($response);
	}
}
else{
	$code = "mod_failed";
	$message = "change to nickname failed";
	array_push($response,array("code"=>$code));
	array_push($response, array("message"=>$message));

	//The array is turned in a JSON object and outputted
	echo json_encode($response);
}
mysqli_close($con);
	
?>