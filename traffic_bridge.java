
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Queue;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class Traffic_Bridge{

    public static final int MAX = 3; // Max number of cars that can cross the bridge in one direction

    public static ArrayList<Integer> arrival_index = new ArrayList<>();
    public static ArrayList<Integer> departure_index = new ArrayList<>();
    private static final Random rand = new Random();

    public static int Direction () {
        return (int)(rand.nextInt(2)); // this will assign to direction a random value ( either 1 for left or 0 for right)
    }


    public static class Semaphore {

        private int count;
        private int direction_semaphore; // The currently allowed direction at that specific time
        private Queue<Vehicle> toLeft; // Those waiting to go left.
        private Queue<Vehicle> toRight; // Those watiting to go right.

        public Semaphore() {
            this.count = MAX;
            this.direction_semaphore = 1; //Initial direction set to left.
            this.toLeft = new LinkedList<>();
            this.toRight = new LinkedList<>();
        }

        public void switchDirection() {
            if (this.direction_semaphore == 1) {
                this.direction_semaphore = 0;
            } else {
                this.direction_semaphore = 1;
            }
        }
        
        public Vehicle unblockSide(int d) {
            Vehicle unblocked;
            if (d == 1) {
                unblocked = this.toLeft.remove();
            } else {
                unblocked = this.toRight.remove();
            }
            unblocked.blocked = false;
            return unblocked;
        }

        public void blockVehicle(Vehicle vehicle) {
            // Enqueue a car that's asking to cross but is not allowed yet.
            vehicle.blocked = true;
            if (vehicle.direction == 1) {
                this.toLeft.add(vehicle);
            } else {
                this.toRight.add(vehicle);
            }
        }

        public boolean queueIsEmpty(boolean same_side) {
            if (same_side) {
                if (this.direction_semaphore == 1) {
                    return (this.toLeft.isEmpty());
                } else {
                    return (this.toRight.isEmpty());
                }
            } else {
                if (this.direction_semaphore == 1) {
                    return (this.toRight.isEmpty());
                } else {
                    return (this.toLeft.isEmpty());
                }
            }
        }

        public void acquireAccess(Vehicle requester) { // this is semWait
            System.out.println(sem);
            this.count--;
            if (this.count >= 0) {
                if (requester.blocked == true) {
                    sem.unblockSide(requester.direction);
                }
                // We still haven't maxed out, just continue.
            } else {
                // count < 0, meaning we maxed out
                if (requester.blocked == false) {
                    this.blockVehicle(requester);
                }
                if (this.queueIsEmpty(false) == false) {
                    // If there is indeed someone on the other side, switch
                    this.switchDirection();
                }
                this.count = MAX;
            }
        }

        public void releaseAccess(Vehicle releaser) { // this is semSignal
            if (this.queueIsEmpty(true)) {
                // If there's no one here, switch.
                this.count = MAX;
                this.switchDirection();
            }
        }

        @Override
        public String toString() {
            return "Count for semaphore: " + this.count; }
        }

    public static class Vehicle extends Thread {

        int vehicle_id;
        int direction;
        int time_to_cross;
        boolean blocked;

        public Vehicle(int vehicle_id, int direction, int time_to_cross) {
            this.vehicle_id = vehicle_id;
            this.direction = direction;
            this.time_to_cross = time_to_cross;
            this.blocked = false;
        }

        public void OneVehicle() {
            this.Arrive();
            this.Cross();
            this.Exit();
        }

        public void Arrive() {
            System.out.println("\nVehicle " + this.toString() + " showed up.");
            arrival_index.add(this.vehicle_id);

            while (true) {
                while (this.direction != sem.direction_semaphore) {
                    System.out.print("");
                    
                    if (this.blocked == false) {
                        sem.blockVehicle(this);
                    }
                }
                sem.acquireAccess(this); //semwait
                if (this.blocked == false) {
                    return;
                }
            }
        }

        public void Cross() {
            System.out.println("\nVehicle " + this.toString() + " is crossing.");
            this.criticalSection(); // just a timeout
        }

        public void Exit() {
            sem.releaseAccess(this);//semsignal
            System.out.println("\nVehicle " + this.toString() + " exited.");
            departure_index.add(this.vehicle_id);
        }

        @Override
        public void run() {
            this.OneVehicle();
        }

        public void criticalSection() {
            int time_to_sleep = 5000;
            long start, end, slept_for;
            boolean interrupted_flag = false;

            System.out.println("Entered critical section");
            while (time_to_sleep > 0) {
                start = System.currentTimeMillis();
                try {
                    Vehicle.sleep(time_to_sleep);
                    break; // If the sleep was uninterrupted, break out of this while loop.
                } catch (InterruptedException ie) {
                    interrupted_flag = true;
                    // we should force a thread back to sleep for the remaining time
                    end = System.currentTimeMillis();
                    slept_for = end - start;// figure out how much sleep time already passed 
                    // then go back to sleep for the remainder
                    time_to_sleep -= slept_for;
                }
            }

            if (interrupted_flag) {
                Thread.currentThread().interrupt();;
            }
        }

        @Override
        public String toString() {
            return "(ID: " + this.vehicle_id + ", Direction: " + this.direction + ")";
        }

    }

    public static Semaphore sem = new Semaphore();

    public static void main(String[] args) throws InterruptedException {

        int choice;

        // create a list of vehicle objects for our 20 vehicles
        List<Vehicle> vehicles = new ArrayList<>();
        // vehicle ids start from 1 to 20 
        // direction is randomly assigned 
        for (int i = 0; i < 20; i++) {
            vehicles.add(i, new Vehicle(i + 1, Direction(), 5));
        }

        //get user choice 
        Scanner inp = new Scanner(System.in);
        System.out.println("Enter your selection:\n1 for test case i\n2 for test case ii \n3 for test case iii");
        choice = inp.nextInt();
        switch (choice) {
            case 1:
                for (int i = 0; i < 5; i++){ //start first 5 threads
                    vehicles.get(i).start();
                    
                }
                TimeUnit.SECONDS.sleep(10); // delay 10 seconds
                for (int i = 5; i < 10; i++) {
                    vehicles.get(i).start();         
                }
                TimeUnit.SECONDS.sleep(10);
                for (int i = 10; i < 15; i++) {
                    vehicles.get(i).start();       
                }
                TimeUnit.SECONDS.sleep(10);
                for (int i = 15; i < 20; i++) {
                    vehicles.get(i).start();
                }
                break;
            case 2:
                for (int i = 0; i < 10; i++) {
                    vehicles.get(i).start();
                }
                TimeUnit.SECONDS.sleep(10);
                for (int i = 10; i < 20; i++) {
                    vehicles.get(i).start();
                }
                break;
            case 3:
                for (int i = 0; i < 20; i++) {
                    vehicles.get(i).start();
                }
                break;

            default:
                System.out.println("Invalid Choice");
        }


        // stoping all threads execution
        for (Vehicle v : vehicles) {
            v.join();
        }

        System.out.println("\nArrival order: " + arrival_index);
        System.out.println("\nDeparture order: " + departure_index);

        System.out.println("\nState of the semaphores in the end: " + sem);

        }

    
}
