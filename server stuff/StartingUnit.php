<?php
/* 	
	This php file send the players the starting positions for the units. 
	Each player starts with one unit on a city square.
*/

//connect to sql database
require "dbConnect.php";

//select the Map table
$sql = "select * from UnitMap;";

$result = mysqli_query($con,$sql);
$response = array();
//if the query's number of rows is greater than 0
if(mysqli_num_rows($result)>0){

$listing = "";	
//fetch the results of the query and store in row for each row until the loop ends
while ($row = mysqli_fetch_assoc($result)) {
	
		//switch statement to encode the UnitID
		switch ($row["UnitID"]) {
		case 0:
			$listing .="A0:A".$row['GridID'].":";
			break;
		case 1:
			$listing .="A1:A".$row['GridID'].":";
			break;
		case 2:
			$listing .="A2:A".$row['GridID'].":";
			break;	
	    case 3:
            $listing .="A3:A".$row['GridID'].":";
            break;
        case 4:
            $listing .="A4:A".$row['GridID'].":";
            break;
        case 5:
            $listing .="A5:A".$row['GridID'].":";
            break;

		}
		//gets rid of the extra delimiter colon at the end of the string
		/*if($row['GridID']!=0){
			$listing = substr_replace($listing,"",-1);
		}*/		
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
	$message = "no UnitID or GridID is found in";
	array_push($response,array('code'=>$code));
	array_push($response,array('Map'=>$message));

	//The code and message is turned in a JSON object and outputted
	echo json_encode($response);
}
mysqli_close($con);
?>
