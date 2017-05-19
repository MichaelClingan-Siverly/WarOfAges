<?php
//connect to sql database
require "dbConnect.php";

$data = file_get_contents('php://input');
$decoded = json_decode($data, true);

$Grid = array('key'=>implode(',',$decoded[0]));
//gets rid of the '[' and ']' using the substring
$GridID = substr($Grid['key'],1);
$GridID = substr($GridID, 0, strlen($GridID)-1);

//create arrays by using comma + space as a delimiter
$GridID = explode(", ", $GridID);

$response = array();
$sql = "DELETE from AdminMap;";

if(mysqli_query($con,$sql)){
        //changing TerrainID into AreaType based on the admin's input
        for($i=0; $i<count($GridID);$i++){
                $sql = "insert into AdminMap values(".$i.", '".$GridID[$i]."');";
                $result = mysqli_query($con,$sql);
        }
		
        $sql="Select * from AdminMap";
        $result= mysqli_query($con,$sql);
        if(mysqli_num_rows($result)==count($GridID)){
                $code = "update_success";
                $message = "The AreaType is updated";
                array_push($response,array("code"=>$code));
                array_push($response,array("message"=>$message));
                //The array is turned in a JSON object and outputted
                echo json_encode($response);
        }
        else{
                $code = "update_failed";
                $message = "The AreaType was not updated";
                array_push($response,array("code"=>$code));
                array_push($response,array("message"=>$message));
                //The code and message is turned in a JSON object and outputted
                echo json_encode($response);
        }
}
else{
$code = "update_failed";
        $message = "The previous map did not delete";
        array_push($response,array("code"=>$code));
        array_push($response,array("message"=>$message));
        //The code and message is turned in a JSON object and outputted
        echo json_encode($response);
}
mysqli_close($con);
?>
