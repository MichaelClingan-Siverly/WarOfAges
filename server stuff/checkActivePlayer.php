<?php
require "dbConnect.php";

$sql = "select userID, loggedin, player from users;";

$result = mysqli_query($con,$sql);
$response = array();
//if the query's number of rows is greater than 0
if(mysqli_num_rows($result)>0){
//fetch the results of the query and store in row for each row until the loop ends
	foreach($result as $row) {
		$userID = $row['userID'];
		$loggedin = $row['loggedin'];
		$player = $row['player'];
		//if user is logged in and is the active player, change to inactive
		if($loggedin == 1 && $player == 1){
			$sql = "update users set player = 0 where userID ='".$userID."'";
			$result = mysqli_query($con,$sql);
		}
		//if user is logged in and is the inactive player, change to active
		else if($loggedin == 1 && $player == 0){
			$sql = "update users set player = 1 where userID ='".$userID."'";
			$result = mysqli_query($con,$sql);
		}
	}	
	//selects the user who is the active player
	$sql = "select userID from users where player = 1";
	$result = mysqli_query($con,$sql);
	if(mysqli_num_rows($result)>0){
		$row = mysqli_fetch_row($result);
		$userID = $row[0];
		$code = "new_turn";
		$message = "".$userID." turn";
		//code is the first object of response array
		array_push($response, array('code' => $code));
		array_push($response, array('message'=>$message));
		// making the array for a JSON object
		echo json_encode($response);
	}
	else{
		$code = "player_failed";
		$message = "Error";
		array_push($response,array('code'=>$code));
		array_push($response,array('message'=>$message));

		//The code and message is turned in a JSON object and outputted
		echo json_encode($response);
	}
}
mysqli_close($con);

?>