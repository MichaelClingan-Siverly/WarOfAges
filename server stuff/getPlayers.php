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
$sql = "select userID from users where userID = '".$userID."' AND loggedin = 1;";
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
                $sql = "UPDATE users SET playerOrder = 1 WHERE userID = '".$userID."'";
                mysqli_query($con,$sql);
                //check if player 1 left the game - player 2 is already assigned.
                if(mysqli_num_rows($result_2) > 0){
                    //Check if player 2 is active, and if not that means this needs to be active
                    $sql = "SELECT userID FROM users WHERE player = 1";
                    $result = mysqli_query($con,$sql);
                    if(mysqli_num_rows == 0){
                        $sql = "UPDATE users SET player = 1 WHERE userID = '".$userID."'";
                        mysqli_query($con,$sql);
                    }
                    $sql = "UPDATE AdminMap SET Owner = '".$userID."' WHERE Owner = 'friendly'";
                    mysqli_query($con,$sql);
                }
                $sql = "UPDATE UnitMap SET userID = '".$userID."' WHERE userID = 'friendly'";
                mysqli_query($con,$sql);
                $code = "player 1 assigned";
        }
        else if(mysqli_num_rows($result_2) == 0 || strcmp($check_result2, $userID) == 0){
                //player two not logged in
                $sql = "UPDATE users SET playerOrder = 2 WHERE loggedIn = 1 AND userID = '".$userID."'";
                mysqli_query($con,$sql);
                $sql = "SELECT GridID FROM AdminMap WHERE Owner = 'friendly'";
                $result = mysqli_query($con,$sql);
                //game has not yet started (first time player 2 joins)
                if(mysqli_num_rows($result) > 0){
                    $sql = "UPDATE users SET player = 1 WHERE playerOrder = 1";
                    mysqli_query($con,$sql);
                    $sql = "UPDATE AdminMap SET Owner = (SELECT userID FROM users WHERE playerOrder = 1) WHERE Owner = 'friendly'";
                    mysqli_query($con,$sql);
                }
                //game has started (p2 is rejoining game)
                else{
                    $sql = "SELECT userID FROM users WHERE player = 1";
                    $result = mysqli_query($con,$sql);
                    //no active player, so I must have been the active one
                    if(mysqli_num_rows($result) == 0){
                        $sql = "UPDATE users SET player = 1 WHERE playerOrder = 2";
                        mysqli_query($con,$sql);
                    }
                }
                $sql = "UPDATE UnitMap SET userID = '".$userID."' WHERE userID = 'hostile'";
                mysqli_query($con,$sql);
                $sql = "UPDATE AdminMap SET Owner = '".$userID."' WHERE Owner = 'hostile'";
                mysqli_query($con,$sql);
                $code = "player 2 assigned";
        }
        else{
                //sucks to be that player. can't have more than two players.
                $code = "no more players can be assigned";
        }
}
else{
        $code = "player not found";
}
array_push($response, array("code"=>$code));
echo json_encode($response);

mysqli_close($con);
?>