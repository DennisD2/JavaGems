package de.spurtikus.testing;

public class ExternalService {

    // Static method
    public static int processStep(int i) {
        System.out.println("You should not see this line !!!!");
        return i++;
    }
}
