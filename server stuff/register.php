<?php
//connect to sql database
require "dbConnect.php";

$data = file_get_contents('php://input');
$decoded = json_decode($data,true);

$userID = $decoded[0]["userID"];
$password = $decoded[1]["password"];


//query statement
$sql = "select * from 'users' where 'userID' = '".$userID."';";

$result = mysqli_query($con,$sql);
$response = array();

//if the query's number of rows is greater than 0
if(mysqli_num_rows($result)>0){
        //the userID is already exists so the user will have to try again
        $code = "reg_failed";
        $message = "User already exists......";
        array_push($response,array("code"=>$code));
		array_push($response,array("message"=>$message));
        // making the array for a JSON object
        echo json_encode($response);
}
else
{
		//if the userID and password are not empty
        if($userID != "" && $password != ""){
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
        else{
                $code = "reg_failed";
                $message = "There is no input";
                array_push($response,array("code"=>$code));
				array_push($response,array("message"=>$message));
                //The array is turned in a JSON object and outputted
                echo json_encode($response);
        }
}
mysqli_close($con);
?>
