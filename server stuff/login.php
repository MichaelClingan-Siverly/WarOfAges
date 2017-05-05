<?php
//connect to sql database
require "dbConnect.php";

$data = file_get_contents('php://input');
$decoded = json_decode($data, true);

$userID = $decoded[0]["userID"];
$password = $decoded[1]["password"];

//query statement
$sql = "select userID from users where userID like'".$userID."' and password like '".$password."';";

$result = mysqli_query($con, $sql);
$response = array();
//if the query's number of rows is greater than 0
if(mysqli_num_rows($result)>0){

	$sql = "update users set loggedin = 1 where userID like'".$userID."' and password like '".$password."';";
    if(mysqli_query($con, $sql)){
		
		// making the array for a JSON object
        $code = "login_success";
        $message =  " '".$userID."' has been logged in";
        array_push($response,array("code"=>$code));
		array_push($response,array("message"=>$message));
        //The array is turned in a JSON object and outputted
        echo json_encode($response);
	}
}
//if the $sql fails this happens
else{
        $code = "login_failed";
        $message = "User not found";
		array_push($response,array("code"=>$code));
		array_push($response,array("message"=>$message));
        //The code and message is turned in a JSON object and outputted
        echo json_encode($response);
}
mysqli_close($con);
?>
