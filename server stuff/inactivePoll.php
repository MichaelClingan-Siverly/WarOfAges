<?php
require "dbConnect.php";

//searching for active player a.k.a. when player = 1
$sql = "select userID from users where player = 1;";
$result = mysqli_query($con,$sql);
$response = array();
$userID = "";
//if the query's number of rows is greater than 0
if(mysqli_num_rows($result)>0){
	foreach($result as $row){
		$userID = $row['userID'];
		//add the active player to json response
		array_push($response,array('userID'=>$userID));
	}
}
else{
	array_push($response,array('userID'=>"null"));
}
$d = array();

//there was a previous if-statement here, but it caused problems when only one player has joined the game
$sql = "select * from UnitMap";
$result = mysqli_query($con,$sql);
foreach($result as $row){
	//store UnitMap inside an array
	$d[] = $row;
}
//add the array of the UnitMap to json response
array_push($response,$d);

echo(json_encode($response));
mysqli_close($con);
?>