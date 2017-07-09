<?php
require "dbConnect.php";
$data = file_get_contents('php://input');
$decoded = json_decode($data, true);

// the first 2 indexes are for active player's unit location and Health
$allyLocation = $decoded[0]["myGridID"];
$allyHealth = $decoded[0]["myUnitHealth"];

// the first 2 indexes are for inactive player's unit location and Health
$enemyLocation = $decoded[1]["enemyGridID"];
$enemyHealth = $decoded[1]["enemyUnitHealth"];

// Getting the userID
$sql = "Select userID from users where playerOrder > 0 and player = 0";
$result = mysqli_query($con,$sql);
$row = mysqli_fetch_row($result);
$enemyID = $row[0];

//I know this is the sender since only the active player can attack
$sql = "Select userID from users where playerOrder > 0 and player = 1";
$result = mysqli_query($con,$sql);
$row = mysqli_fetch_row($result);
$userID = $row[0];

//Previously it only updated a units healts if neither died. Need to do it no matter what
$sql = "Update UnitMap set health = ".$allyHealth.", attacked = 1 where userID = '".$userID."' and GridID = ".$allyLocation."";
$sql2 = "Update UnitMap set health = ".$enemyHealth." where userID = '".$enemyID."' and GridID = ".$enemyLocation."";
mysqli_query($con,$sql);
mysqli_query($con,$sql2);

// if neither died from attacks update the health
if($allyHealth > 0 && $enemyHealth > 0){
        $code = "attack_success";
        $message = "Both units health are updated";
        array_push($response, array("code"=>$code));
        array_push($response, array("message"=>$message));
}
else if($allyHealth <= 0 && $enemyHealth <= 0 ){
        $sql = "DELETE from UnitMap where GridID = ".$allyLocation." AND userID = '".$userID."'";
        mysqli_query($con,$sql);
        $sql = "DELETE from UnitMap where GridID = ".$enemyLocation." AND userID = '".$enemyID."'";
        mysqli_query($con,$sql);
        
        $code = "attack_success";
        $message = "Both units have died";
        array_push($response, array("code"=>$code));
        array_push($response, array("message"=>$message));
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
