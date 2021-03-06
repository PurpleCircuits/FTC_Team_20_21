package org.firstinspires.ftc.teamcode;

import com.qualcomm.hardware.bosch.BNO055IMU;
import com.qualcomm.hardware.rev.Rev2mDistanceSensor;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.DigitalChannel;
import com.qualcomm.robotcore.hardware.DistanceSensor;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.AxesOrder;
import org.firstinspires.ftc.robotcore.external.navigation.AxesReference;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.robotcore.external.navigation.Orientation;

/**
 * Most of this code was copied from the FTC examples, but we tweaked it for our purposes.
 *
 * This makes the robot drive forward and turn left and go forward, parking under the bridge during
 * autonomous gaining us 5 points.
 */
@Autonomous(name = "PurpleSensorsTest", group = "Linear Opmode")
public class PurpleSensorsTest extends LinearOpMode {
    static final double     COUNTS_PER_MOTOR_REV    = 1120 ;    // (40 GEARBOX) eg: TETRIX Motor Encoder
    static final double     DRIVE_GEAR_REDUCTION    = 1.0 ;     // This is < 1.0 if geared UP
    static final double     WHEEL_DIAMETER_INCHES   = 4 ;     // For figuring circumference
    static final double     COUNTS_PER_INCH         = (COUNTS_PER_MOTOR_REV * DRIVE_GEAR_REDUCTION) / (WHEEL_DIAMETER_INCHES * 3.1415);
    private static final double SERVO_MIN_POS = 0.0; // Minimum rotational position
    private static final double SERVO_MAX_POS = 1.0; // Maximum rotational position

    // The speed for the drive motors to operate at during autonomous
    private static final double SPEED = 0.1;

    // Declare hardware
    private DcMotor leftDrive = null;
    private DcMotor rightDrive = null;
    private DcMotor theClawMotor = null;
    private Servo theClawServo = null;
    private BNO055IMU imu = null;
    private ElapsedTime runtime = new ElapsedTime();
    private DigitalChannel digitalTouch = null;

    //private DistanceSensor topDistanceSensor = null;
    //private Rev2mDistanceSensor sensorTimeOfFlight = null;
    //private DistanceSensor bottomDistanceSensor = null;

    // Used for determining how long something has ran

    /**
     * This is the entry of our Op Mode.
     */
    @Override
    public void runOpMode() {
        telemetry.addData("Status", "PurpleSensorsTest");
        telemetry.update();
        initHardware();

        // Wait for the game to start (driver presses PLAY)
        waitForStart();
        runtime.reset();

            encoderDrive(.3,24,24, 5);
            turnLeft(90,10);
            turnRight(270,10);
            dropGoal();
        telemetry.update();
    }

    /*
     * A simple method used to make our robot reverse or go forward.
     *
     * @param time The amount of time in seconds to drive for
     * @param forward True to go forward, false to go background

    private void driveFor(double time, boolean forward) {
        // Determine the direction and power to set
        if (forward) {
            leftDrive.setPower(SPEED);
            rightDrive.setPower(SPEED);
        } else {
            leftDrive.setPower(-SPEED);
            rightDrive.setPower(-SPEED);
        }
        // Reset the timeout time and start motion
        runtime.reset();
        while (opModeIsActive() && (runtime.seconds() < time)) {
            //distanceAction();
            telemetry.update();
        }
        // Stop all motion
        leftDrive.setPower(0);
        rightDrive.setPower(0);
    }
    */
    //TODO the below method is almost exact duplicate of the turnRight, minus the degrees left and setPower calls. can this be broken up differently?
    public void turnLeft(double turnAngle, double timeoutS) {
        if (!opModeIsActive()){
            return;
        }
        Orientation angles = imu.getAngularOrientation(AxesReference.INTRINSIC, AxesOrder.ZYX, AngleUnit.DEGREES);
        double speed=.5;
        double scaledSpeed=speed;
        double targetHeading=angles.firstAngle+turnAngle;
        if(targetHeading<-180) {targetHeading+=360;}
        if(targetHeading>180){targetHeading-=360;}
        double degreesRemaining = ((int)(Math.signum(angles.firstAngle-targetHeading)+1)/2)*(360-Math.abs(angles.firstAngle-targetHeading))
                + (int)(Math.signum(targetHeading-angles.firstAngle)+1)/2*Math.abs(angles.firstAngle-targetHeading);
        runtime.reset();
        while(opModeIsActive() && runtime.seconds() < timeoutS && degreesRemaining>1)
        {
            //TODO maybe change the 100 to 75 to make the turn slightly faster.
            //TODO change this is TestSensorsTest also
            scaledSpeed=degreesRemaining/(50+degreesRemaining)*speed;
            if(scaledSpeed>1 || scaledSpeed<.3){scaledSpeed=.3;}//We have a minimum and maximum scaled speed

            leftDrive.setPower(scaledSpeed);
            rightDrive.setPower(-1*scaledSpeed);
            angles = imu.getAngularOrientation(AxesReference.INTRINSIC, AxesOrder.ZYX, AngleUnit.DEGREES);
            degreesRemaining = ((int)(Math.signum(angles.firstAngle-targetHeading)+1)/2)*(360-Math.abs(angles.firstAngle-targetHeading))
                    + (int)(Math.signum(targetHeading-angles.firstAngle)+1)/2*Math.abs(angles.firstAngle-targetHeading);
        }
        leftDrive.setPower(0);
        rightDrive.setPower(0);
    }
    //TODO see comments in turnLeft
    public void turnRight(double turnAngle, double timeoutS) {
        if (!opModeIsActive()){
            return;
        }
        Orientation angles = imu.getAngularOrientation(AxesReference.INTRINSIC, AxesOrder.ZYX, AngleUnit.DEGREES);
        double speed=.5;
        double scaledSpeed=speed;
        double targetHeading=angles.firstAngle+turnAngle;
        if(targetHeading<-180) {targetHeading+=360;}
        if(targetHeading>180){targetHeading-=360;}
        double degreesLeft = ((int)(Math.signum(targetHeading-angles.firstAngle)+1)/2)*(360-Math.abs(angles.firstAngle-targetHeading))
                + (int)(Math.signum(angles.firstAngle-targetHeading)+1)/2*Math.abs(angles.firstAngle-targetHeading);
        runtime.reset();
        while (opModeIsActive() && runtime.seconds() < timeoutS && degreesLeft>1)
        {
            scaledSpeed=degreesLeft/(50+degreesLeft)*speed;
            if(scaledSpeed>1 || scaledSpeed<.3){scaledSpeed=.3;}

            leftDrive.setPower(-1*scaledSpeed);
            rightDrive.setPower(scaledSpeed);
            angles = imu.getAngularOrientation(AxesReference.INTRINSIC, AxesOrder.ZYX, AngleUnit.DEGREES);
            degreesLeft = ((int)(Math.signum(targetHeading-angles.firstAngle)+1)/2)*(360-Math.abs(angles.firstAngle-targetHeading))
                    + (int)(Math.signum(angles.firstAngle-targetHeading)+1)/2*Math.abs(angles.firstAngle-targetHeading);
        }
        leftDrive.setPower(0);
        rightDrive.setPower(0);
    }

    /*
     * A simple method used to turn our robot.
     *
     * @param time The amount of time in seconds to execute a turn for
     * @param right True to turn right, false to turn left

    private void turnFor(double time, boolean right) {
        // Determine the direction and power to set
        if (right) {
            leftDrive.setPower(SPEED);
            rightDrive.setPower(-SPEED);
        } else {
            leftDrive.setPower(-SPEED);
            rightDrive.setPower(SPEED);
        }
        // Reset the timeout time and start motion
        runtime.reset();
        while (opModeIsActive() && (runtime.seconds() < time));
        // Stop all motion
        leftDrive.setPower(0);
        rightDrive.setPower(0);
    }
*/
    /**
     * Simply initializes our hardware from the FTC config into variables.
     */
    private void initHardware() {
        // Initialize the hardware variables. Note that the strings used here as parameters
        // to 'get' must correspond to the names assigned during the robot configuration
        // step (using the FTC Robot Controller app on the phone).
        leftDrive = hardwareMap.get(DcMotor.class, "left_drive");
        rightDrive = hardwareMap.get(DcMotor.class, "right_drive");
        theClawMotor = hardwareMap.get(DcMotor.class, "the_claw_motor");
        theClawServo = hardwareMap.get(Servo.class, "the_claw_servo");

        digitalTouch = hardwareMap.get(DigitalChannel.class, "limit_sensor");
        digitalTouch.setMode(DigitalChannel.Mode.INPUT);

        // Our robot needs the motor on one side to be reversed to drive forward
        leftDrive.setDirection(DcMotor.Direction.REVERSE);
        rightDrive.setDirection(DcMotor.Direction.FORWARD);

        // Ensure to not run with encoder //TODO add back in after done with encoders (if not using)
        //leftDrive.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        //rightDrive.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);


        // We are expecting the IMU to be attached to an I2C port on a Core Device Interface Module and named "imu".
        BNO055IMU.Parameters parameters = new BNO055IMU.Parameters();
        parameters.loggingEnabled = true;
        parameters.loggingTag     = "IMU";
        imu = hardwareMap.get(BNO055IMU.class, "imu");
        imu.initialize(parameters);

        //bottomDistanceSensor = hardwareMap.get(DistanceSensor.class, "bottom_distance");
        //topDistanceSensor = hardwareMap.get(DistanceSensor.class, "top_distance");

        // Log that init hardware is finished
        telemetry.log().clear();
        telemetry.log().add("Init. hardware finished.");
        telemetry.clear();
        telemetry.update();
    }
    /*
    private void distanceAction(){
        // generic DistanceSensor methods.
        telemetry.addData("Bottom deviceName", bottomDistanceSensor.getDeviceName() );
        telemetry.addData("range", String.format("%.01f cm", bottomDistanceSensor.getDistance(DistanceUnit.CM)));
        telemetry.addData("Top deviceName", topDistanceSensor.getDeviceName() );
        telemetry.addData("range", String.format("%.01f cm", topDistanceSensor.getDistance(DistanceUnit.CM)));

    }
     */
    /*
     *  Method to perform a relative move, based on encoder counts.
     *  Encoders are not reset as the move is based on the current position.
     *  Move will stop if any of three conditions occur:
     *  1) Move gets to the desired position
     *  2) Move runs out of time
     *  3) Driver stops the opmode running.
     */
    public void encoderDrive(double speed,
                             double leftInches, double rightInches,
                             double timeoutS) {
        if (!opModeIsActive()){
            return;
        }
        int newLeftTarget;
        int newRightTarget;

        // Ensure that the opmode is still active
        if (opModeIsActive()) {

            // Determine new target position, and pass to motor controller
            newLeftTarget = leftDrive.getCurrentPosition() + (int)(leftInches * COUNTS_PER_INCH);
            newRightTarget = rightDrive.getCurrentPosition() + (int)(rightInches * COUNTS_PER_INCH);
            leftDrive.setTargetPosition(newLeftTarget);
            rightDrive.setTargetPosition(newRightTarget);

            // Turn On RUN_TO_POSITION
            leftDrive.setMode(DcMotor.RunMode.RUN_TO_POSITION);
            rightDrive.setMode(DcMotor.RunMode.RUN_TO_POSITION);

            // reset the timeout time and start motion.
            runtime.reset();
            leftDrive.setPower(Math.abs(speed));
            rightDrive.setPower(Math.abs(speed));

            // keep looping while we are still active, and there is time left, and both motors are running.
            // Note: We use (isBusy() && isBusy()) in the loop test, which means that when EITHER motor hits
            // its target position, the motion will stop.  This is "safer" in the event that the robot will
            // always end the motion as soon as possible.
            // However, if you require that BOTH motors have finished their moves before the robot continues
            // onto the next step, use (isBusy() || isBusy()) in the loop test.
            while (opModeIsActive() &&
                    (runtime.seconds() < timeoutS) &&
                    (leftDrive.isBusy() && rightDrive.isBusy())) {

                // Display it for the driver.
                telemetry.addData("Path1",  "Running to %7d :%7d", newLeftTarget,  newRightTarget);
                telemetry.addData("Path2",  "Running at %7d :%7d",
                        leftDrive.getCurrentPosition(),
                        rightDrive.getCurrentPosition());
                telemetry.update();
            }

            // Stop all motion;
            leftDrive.setPower(0);
            rightDrive.setPower(0);

            // Turn off RUN_TO_POSITION
            leftDrive.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
            rightDrive.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

            //  sleep(250);   // optional pause after each move
        }
    }

    private void dropGoal() {
        if (!opModeIsActive()) {
            return;
        }
        if(isAtLimit()) {
            //Arm down until sensor
            theClawMotor.setPower(-.3);
            while(isAtLimit()){
            }
            theClawMotor.setPower(0);
        }
        theClawServo.setPosition(SERVO_MAX_POS);
        //Arm Up until sensor
        theClawMotor.setPower(.5);
        sleep(500);
        theClawMotor.setPower(0);
    }
    private boolean isAtLimit(){
        // send the info back to driver station using telemetry function.
        // if the digital channel returns true it's HIGH and the button is unpressed.
        return digitalTouch.getState();
    }
}