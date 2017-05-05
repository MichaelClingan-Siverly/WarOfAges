<?php
require "dbConnect.php";
$data = file_get_contents('php://input');
$decoded = json_decode($data, true);

// the first 2 indexes are for active player's unit location and Health
$allyLocation = $decoded[0]["myUnitID"];
$allyHealth = $decoded[1]["myUnitHealth"];

// the first 2 indexes are for inactive player's unit location and Health
$enemyLocation = $decoded[2]["enemyUnitID"];
$enemyHealth = $decoded[3]["enemyUnitHealth"];

// Getting the userID
$sql = "Select userID from users where playerOrder = 1";
$result = mysqli_query($con,$sql);
$row = mysqli_fetch_row($result);
$enemyID = $row[0];

$sql = "Select userID from users where playerOrder = 2";
$result = mysqli_query($con,$sql);
$row = mysqli_fetch_row($result);
$userID = $row[0];

// if neither died from attacks update the health
if($allyHealth > 0 && $enemyHealth > 0){
        $sql = "Update UnitMap set health = ".$allyHealth." where userID = '".$userID."'";
        $sql2 = "Update UnitMap set health = ".$enemyHealth." where userID = '".$enemyID."'";
        mysqli_query($con,$sql);
        mysqli_query($con,$sql2);

        if(mysqli_query($con,$sql) && mysqli_query($con,$sql2)){
                $code = "attack_success";
                $message = "Both units health are updated";
                array_push($response, array("code"=>$code));
                array_push($response, array("message"=>$message));
        }
}
// if the attacker died, delete from table
else if($allyHealth <= 0){
        $sql = "DELETE from UnitMap where GridID = ".$allyLocation." AND userID = '".$userID."'";


        if(mysqli_query($con,$sql)){
                $code = "attack_failed";
                $message = "The attacking unit has died";
                array_push($response, array("code"=>$code));
                array_push($response, array("message"=>$message));
        }
}
// if the defender died, delete from table
else if ($enemyHealth <=0){
        $sql = "DELETE from UnitMap where GridID = ".$enemyLocation." AND userID = '".$enemyID."'";
        if(mysqli_query($con,$sql)){
                $code = "attack_success";
                $message = "The defending unit has died";
                array_push($response, array("code"=>$code));
                array_push($response, array("message"=>$message));
        }
        else{
                $code = "attack_failed";
                $message = "An error has occurred";
                array_push($response, array("code"=>$code));
                array_push($response, array("message"=>$message));
        }
}
echo json_encode($response);
mysqli_close($con);
?>
