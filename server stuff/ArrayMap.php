<?php
//connect to sql database
require "dbConnect.php";

$data = file_get_contents('php://input');
$decoded = json_decode($data, true);

$MapLocation = $decoded[0]["GridID"];
//selecting the terrain description based on the location given
$sql = "select T.terr_description from Map M, Terrain T where M.GridID like'".$GridID."'AND T.AreaType=M.AreaType;";

$result = mysqli_query($con,$sql);
$response = array();

//if the query's number of rows is greater than 0
if(mysqli_num_rows($result)>0){
	$row = mysqli_fetch_row($result);
	$des = $row[0];
	$code = "description_found";
	array_push($response,array("code"=>$code));
	array_push($response,array("des"=>$des));
	//The array is turned in a JSON object and outputted
	echo json_encode($response);
}
else{
	$code = "Query_failed";
	$message = "no description found at ".$GridID."";
	array_push($response,array("code"=>$code));
	array_push($response,array("des"=>$message));
	//The code and message is turned in a JSON object and outputted
	echo json_encode($response);
}
mysqli_close($con);
?>
