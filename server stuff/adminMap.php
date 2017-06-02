<?php
//connect to sql database
require "dbConnect.php";

//select the AdminMap table
$sql = "SELECT * FROM AdminMap ORDER BY GridID Asc;";

$result = mysqli_query($con,$sql);
$response = array();
//if the query's number of rows is greater than 0
if(mysqli_num_rows($result)>0){
        
    $p1Start;
    $p2Start;
    
    $size = mysqli_num_rows($result);
    $listing = "".$size.":";
    
    file_put_contents("temp.txt", $size);
    //fetch the results of the query and store in row for each row until the loop ends
    while ($row = mysqli_fetch_assoc($result)) {
    
        //switch statement to encode the AreaType. I'm using numbers because it's less information than a bunch of Strings
        //May end up sending Strings along, since they won't require as much format coordination between server and client
        switch ($row["AreaType"]) {
            //if desert return 1
            case "desert":
                $listing .="1:";
                break;
            //if forest return 2
            case "forest":
                $listing .="2:";
                break;
            //if meadow return 3
            case "meadow":
                $listing .="3:";
                break;
            //if mountain return 4
            case "mountain":
                $listing .="4:";
                break;
            //5      
            case "town_friendly_start":
                $p1Start = $row['GridID'];
            case "town_friendly":
                $listing .="5:";
                break;
            //6
            case "town_hostile_start":
                $p2Start = $row['GridID'];
            case "town_hostile":
                $listing .="6:";
                break;
            //7
            case "town_neutral":
                $listing .="7:";
                break;
            //if pond return 8
            case "water":
                $listing .="8:";
                break;
        }
    }
    //gets rid of the extra delimiter colon at the end of the string
    if(mysqli_num_rows($result)==$row["GridID"]){
        $listing = substr_replace($listing,"",-1);
    }
    
    $size = $size - 1;
    $sql = "SELECT * FROM UnitMap";
    $result = mysqli_query($con,$sql);
    if(mysqli_num_rows($result)==0){
        $sql = "INSERT INTO UnitMap VALUES (".$p1Start.", 5, 2000, NULL)";
        mysqli_query($con,$sql);
        $sql = "INSERT INTO UnitMap VALUES (".$p2Start.", 5, 2000, NULL)";
        mysqli_query($con,$sql);
    }
    
    $code = "update_success";
    //code is the first object of response array
    array_push($response, array('code' => $code));
    //Map is the second object of response array
    array_push($response, array('Map'=>$listing));
    // making the array for a JSON object
    echo json_encode($response);
}
else{
    $code = "update_failed";
    $message = "no AreaType or GridID is found in";
    array_push($response,array('code'=>$code));
    array_push($response,array('Map'=>$message));

    //The code and message is turned in a JSON object and outputted
    echo json_encode($response);
}
mysqli_close($con);
?>
