<?php
require "dbConnect.php";

/*
 * Takes JSONArray from client containing a userID of player
 * Checks a file containing the number of players, not spectators.
 * Returns "player 1 (or 2) assigned" code if the user has been assigned as a player
 * Returns "player not assigned" code is user could not be assigneid
 * Returns "player_not_found" code if either userID was not found or userID was not logged in
 */

//get a userID from the client
$data = file_get_contents('php://input');
$decoded = json_decode($data, true);
$userID = $decoded[0]['userID'];
$response = array();

//query DB if that user exists and is logged in
$sql = "select userID from users where userID like '".$userID."' AND loggedin = 1;";
$result = mysqli_query($con,$sql);

//if the query's number of rows is greater than 0
if(mysqli_num_rows($result)>0){
        $sql = "SELECT userID FROM users WHERE playerOrder = 1;";
        $sql_2 = "SELECT userID FROM users WHERE playerOrder = 2;";
        $result = mysqli_query($con,$sql);
        $result_2 = mysqli_query($con,$sql_2);
        $check_result = mysqli_fetch_array($result)[0];
        $check_result2 = mysqli_fetch_array($result_2)[0];
        if(mysqli_num_rows($result) == 0 || strcmp($check_result, $userID) == 0){
                //player one not logged in
                $sql = "UPDATE users SET player = 0, playerOrder = 1 WHERE userID like '".$userID."'";
                mysqli_query($con,$sql);
                $sql = "UPDATE UnitMap SET userID = '".$userID."' WHERE GridID = 0";
                mysqli_query($con,$sql);
                $code = "player 1 assigned";
}
        else if(mysqli_num_rows($result_2) == 0 || strcmp($check_result2, $userID) == 0){
                //player two not logged in
                $sql = "UPDATE users SET player = 1, playerOrder = 2 WHERE loggedIn = 1 AND userID like '".$userID."'";
                $result = mysqli_query($con,$sql);
                //I'm not sure where the end of the map is, so I just check for any unit other than at gridID 0
                $sql = "UPDATE UnitMap SET userID = '".$userID."' WHERE GridID > 0";
                $result = mysqli_query($con,$sql);
                $code = "player 2 assigned";
        }
        else{
                //sucks to be that player. can't have more than two players.
                $code = "player not assigned";
        }
}
else{
        $code = "player_not_found";
}
array_push($response, array("code"=>$code));
echo json_encode($response);

mysqli_close($con);
?>



