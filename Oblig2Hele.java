import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

// Oppgave 12: Fult program

public class Oblig2Hele {
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Bruk: java Oblig2Hele <sti_til_mappen_med_datafiler>");
            return;
        }

        String stiTilMappe = args[0];
        Monitor2 monitorSyk = new Monitor2();
        Monitor2 monitorFrisk = new Monitor2();
        ArrayList<Thread> tradListe = new ArrayList<>();
        ArrayList<Thread> fletteTradListeSyk = new ArrayList<>();
        ArrayList<Thread> fletteTradListeFrisk = new ArrayList<>();

        File metafil = new File(stiTilMappe + "/metadata.csv");

        try (Scanner les = new Scanner(metafil)) {
            while (les.hasNextLine()) {
                String linje = les.nextLine();
                if (!linje.isEmpty()) {
                    // Splitter linjen på tabulatoren (\t) siden vi har både filnanvet og en boolean verdi å lese i en tabell
                    String[] deler = linje.split("\t"); 
                    String filnavn = deler[0].trim();                                // første del er filnavnet
                    boolean harSykdom = Boolean.parseBoolean(deler[1].trim());       // andre delen er den boolske verdien (Hvorfor out of bounds her??)
                    Monitor2 monitor = harSykdom ? monitorSyk : monitorFrisk;        // legger inn i monitorer for syke og friske
                    Thread leseTrad = new Thread(new LeseTrad(stiTilMappe + "/" + filnavn, monitor));
                    tradListe.add(leseTrad);
                    leseTrad.start();
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("Metadata-filen ble ikke funnet: " + e.getMessage());        
        }

        // Venter på at alle lesetrådene skal fullføre
        try {
            for (Thread thread : tradListe) {
                thread.join();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Oppretter og starter 8 flettetråder for syke personer
        int antallTraader = 6;
        for (int i = 0; i < antallTraader; i++) {
            Thread fletteTrad = new Thread(new FletteTrad(monitorSyk));
            fletteTradListeSyk.add(fletteTrad);
            fletteTrad.start();
        }

        // Oppretter og starter 8 flettetråder for friske personer
        for (int i = 0; i < antallTraader; i++) {
            Thread fletteTrad = new Thread(new FletteTrad(monitorFrisk));
            fletteTradListeFrisk.add(fletteTrad);
            fletteTrad.start();
        }

        // Venter på at alle flettetrådene for syke personer skal fullføre
        try {
            for (Thread fletteTrad : fletteTradListeSyk) {
                fletteTrad.join();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Venter på at alle flettetrådene for friske personer skal fullføre
        try {
            for (Thread fletteTrad : fletteTradListeFrisk) {
                fletteTrad.join();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Lagrer de siste hashmapene for syke og friske personer 
         HashMap<String, Subsekvens> sisteHashMapSyk = monitorSyk.taUtHashMap(0);
         HashMap<String, Subsekvens> sisteHashMapFrisk = monitorFrisk.taUtHashMap(0);

        // Finner og skriver ut dominante subsekvenser
        ArrayList<String> dominanteSubsekvenser = new ArrayList<>();

        for (String subsekvensSyk : sisteHashMapSyk.keySet()) {                      // løper gjennom hver subsekvens i hashmap for de syke
            if (sisteHashMapFrisk.containsKey(subsekvensSyk)) {                      // sjekker for subsekvenser som er felles i hashmapene

                int forekomsterSyk = sisteHashMapSyk.get(subsekvensSyk).hentAntall();       
                int forekomsterFrisk = sisteHashMapFrisk.get(subsekvensSyk).hentAntall();
                int differanse = forekomsterSyk - forekomsterFrisk;                  // sammenligner hvor stor differanse det er i forekomster for subsekvensene

                if (differanse >= 7) {
                    dominanteSubsekvenser.add(subsekvensSyk);                        // dersom den forekommer minst 7 ganger eller mer, legges til i listen.
                }
         }
    }

        System.out.println("Dominante Subsekvenser for smittede:");
        for (String subsekvens : dominanteSubsekvenser) {                           // løper over listen med dominante subsekvenser
            int antallSyk = sisteHashMapSyk.get(subsekvens).hentAntall();           // henter ut antallene hos syke
            int antallFrisk = sisteHashMapFrisk.get(subsekvens).hentAntall();       // henter ut antallene hos friske
            int differanse = antallSyk - antallFrisk;                               // regner ut differansen

            System.out.println("- " + subsekvens + " (" + differanse + " flere forekomster)");    // printer ut resultatene
        }   
     }
}

