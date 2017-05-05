<?php
//connect to sql database
require "dbConnect.php";

$data = file_get_contents('php://input');
$decoded = json_decode($data, true);

$Grid = array('key'=>implode(',',$decoded[0]));
$Area = array('key1'=>implode(',',$decoded[1]));
//gets rid of the '[' and ']' using the substring
$GridID = substr($Grid['key'],1);
$GridID = substr($GridID, 0, strlen($GridID)-1);
$AreaType = substr($Area['key1'],1);
$AreaType = substr($AreaType,0, strlen($AreaType)-1);

//create arrays by using comma + space as a delimiter
$GridID = explode(", ", $GridID);
$AreaType = explode(", ", $AreaType);

$response = array();
$sql = "DELETE from AdminMap;";

if(mysqli_query($con,$sql)){
        //changing TerrainID into AreaType based on the admin's input
        for($i=0; $i<count($GridID);$i++){
                $terrain="";
                switch($AreaType[$i]){
                case 0:
                        $terrain = "desert";
                        break;
                case 1:
                        $terrain = "forest";
                        break;
                case 2:
                        $terrain = "meadow";
                        break;
				case 3:
                        $terrain = "mountain";
                        break;
				case 4:
                        $terrain = "town";
                        break;
				case 5:
                        $terrain = "pond";
                        break;
                }

                $sql = "insert into AdminMap values(".$GridID[$i].", '".$terrain."');";
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
