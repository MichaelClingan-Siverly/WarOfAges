<?php
//connect to sql database
require "dbConnect.php";

//select the Map table
$sql = "select * from Map;";

$result = mysqli_query($con,$sql);
$response = array();
//if the query's number of rows is greater than 0
if(mysqli_num_rows($result)>0){

$listing = "";	
//fetch the results of the query and store in row for each row until the loop ends
while ($row = mysqli_fetch_assoc($result)) {
	
		//switch statement to encode the AreaType
		switch ($row["AreaType"]) {
			//if desert return 1
		case "desert":
			$listing .="A1:A".$row['GridID'].":";
			break;
			//if forest return 2
		case "forest":
			$listing .="A2:A".$row['GridID'].":";
			break;
			//if meadow return 3
		case "meadow":
			$listing .="A3:A".$row['GridID'].":";
			break;
			//if mountain return 4
		case "mountain":
			$listing .="A4:A".$row['GridID'].":";
			break;
			//if town return 5
		case "town":
			$listing .="A5:A".$row['GridID'].":";
			break;
			//if pond return 6
		case "pond":
			$listing .="A6:A".$row['GridID'].":";
			break;	
		}
		//gets rid of the extra delimiter colon at the end of the string
		if(mysqli_num_rows($result)==$row["GridID"]){
			$listing = substr_replace($listing,"",-1);
		}
		
    }
	$code = "Query_success";
	//code is the first object of response array
	array_push($response, array('code' => $code));
	//Map is the second object of response array
	array_push($response, array('Map'=>$listing));
	// making the array for a JSON object
	echo json_encode($response);
}
else{
	$code = "Query_failed";
	$message = "no AreaType or GridID is found in";
	array_push($response,array('code'=>$code));
	array_push($response,array('Map'=>$message));

	//The code and message is turned in a JSON object and outputted
	echo json_encode($response);
}
mysqli_close($con);
?>
