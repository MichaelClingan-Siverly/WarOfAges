<?php
//connect to sql database
require "dbConnect.php";

$data = file_get_contents('php://input');
$decoded = json_decode($data,true);

$userID = $decoded[0]["userID"];
$password = $decoded[1]["password"];

$response = array();



//make sure the inputs are not null or only whitespace
if($userID == NULL || $password == NULL || ctype_space($userID) || ctype_space($password)){
    $code = "reg_failed";
    $message = "there is no input";
        array_push($response,array("code"=>$code));
		array_push($response,array("message"=>$message));
        // making the array for a JSON object
        echo json_encode($response);
}
else{
    //query statement
    $sql = "select userID from users where userID = '".$userID."';";
    $result = mysqli_query($con,$sql);

    //if the query's number of rows is greater than 0
    if(mysqli_num_rows($result)>0){
        //the userID is already exists so the user will have to try again
        $code = "reg_failed";
        $message = "User already exists";
        array_push($response,array("code"=>$code));
		array_push($response,array("message"=>$message));
        // making the array for a JSON object
        echo json_encode($response);
    }
    else{
        
        //the userID and password must not be empty, since I checked it already
        //inserted the values into the users table
        $sql = "insert into users values('".$userID."', NULL,'".$password."', 0, 0, 0)";
        $result = mysqli_query($con,$sql);
        $code = "reg_success";
        $message = "User has been registered";
        array_push($response,array("code"=>$code));
	    array_push($response,array("message"=>$message));
        //The array is turned in a JSON object and outputted
        echo json_encode($response);
    }
}
mysqli_close($con);
?>
