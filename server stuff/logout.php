<?php
require "dbConnect.php";
$data = file_get_contents('php://input');
$decoded = json_decode($data, true);
$userID = $decoded[0]['userID'];
$response = array();
$sql = "SELECT playerOrder FROM users WHERE userID = '".$userID."'";
$result = mysqli_query($con, $sql);
$firstRow = mysqli_fetch_assoc($result);
//So this suddenly stopped working...the $firstRow == 1 didn't work anymore. got it working again with the reset
if(mysqli_num_rows($result)>0 && reset($firstRow) == 1){
    $sql = "UPDATE AdminMap SET Owner = 'friendly' WHERE Owner = '".$userID."'";
    mysqli_query($con,$sql);
    $sql = "UPDATE UnitMap SET userID = 'friendly' WHERE userID = '".$userID."'";
    mysqli_query($con,$sql);
}
else if(mysqli_num_rows($result)>0 && reset($firstRow) == 2){
    $sql = "UPDATE AdminMap SET Owner = 'hostile' WHERE Owner = '".$userID."'";
    mysqli_query($con,$sql);
    $sql = "UPDATE UnitMap SET userID = 'hostile' WHERE userID = '".$userID."'";
    mysqli_query($con,$sql);
    $code = "Game_over";
        $message = "second if";
        //code is the first object of response array
        array_push($response, array('code' => $code));
        //Map is the second object of response array
        array_push($response, array('message'=>$message));
        // making the array for a JSON object
        echo json_encode($response);
}
$sql = "SELECT loggedIn FROM users WHERE userID like '".$userID."'";
$result = mysqli_query($con,$sql);
if(mysqli_num_rows($result)>0){
        $sql = "UPDATE users SET  playerOrder = 0, player = 0 WHERE userID like '".$userID."'";
        mysqli_query($con,$sql);
        $sql = "SELECT * FROM users WHERE playerOrder > 0";
        $result = mysqli_query($con,$sql);
        //no players are now in the game
        if(mysqli_num_rows($result)==0){
                $sql = "DELETE FROM UnitMap";
                mysqli_query($con,$sql);
                $sql = "UPDATE AdminMap SET Owner = 'hostile' WHERE AreaType = 'town_hostile_start' OR AreaType = 'town_hostile'";
                mysqli_query($con,$sql);
                $sql = "UPDATE AdminMap SET Owner = 'friendly' WHERE AreaType = 'town_friendly_start' OR AreaType = 'town_friendly";
                mysqli_query($con,$sql);
                $sql = "UPDATE AdminMap SET Owner = 'neutral' WHERE AreaType = 'town_neutral'";
                mysqli_query($con,$sql);
        }
} 
//logging out will make them not logged in anymore
$sql = "UPDATE users SET loggedin = 0 WHERE userID like '".$userID."'";
if(mysqli_query($con,$sql)){
        $code = "Game_over";
        $message = "Game Over";
        //code is the first object of response array
        array_push($response, array('code' => $code));
        //Map is the second object of response array
        array_push($response, array('message'=>$message));
        // making the array for a JSON object
        echo json_encode($response);
}
else{
        $code = "Error";
        $message = "SQL Error has occured";
        //code is the first object of response array
        array_push($response, array('code' => $code));
        //Map is the second object of response array
        array_push($response, array('message'=>$message));
        // making the array for a JSON object
        echo json_encode($response);
        }
?>