public class SkatConfig {

    public boolean pflichtRamsch = true;
    public int beginnRamsch = 1;
    public int wiederholRamsch = 3;

    public SkatConfig(){

    }

    public boolean isPflichtRamsch() {
        return pflichtRamsch;
    }

    public int getBeginnRamsch() {
        return beginnRamsch;
    }

    public int getWiederholRamsch() {
        return wiederholRamsch;
    }
}
