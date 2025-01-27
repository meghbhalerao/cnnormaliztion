/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tool;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import tool.sieves.Sieve;
import tool.util.Abbreviation;
import tool.util.AmbiguityResolution;
import tool.util.Concept;
import tool.util.DocumentConcepts;
import tool.util.Documents;
import tool.util.Ling;
import tool.util.Terminology;
import tool.util.Util;
import java.io.*;
import java.io.FileWriter; 

/**
 *
 * @author 
 */
public class Main {
    
    public static File training_data_dir;
    public static File test_data_dir;
    MultiPassSieveNormalizer multiPassSieve;
    PassAllSievesNormalizer allPassSieve;
    public static File output_data_dir;
    
    /**
     *
     * @param args
     * @throws IOException
     */
    public Main(String[] args) throws IOException {
        init(args);
    }

    public static void writeCandidateFileLine (FileWriter writer, Concept concept) throws IOException
    { 
        int sieveLevel = concept.getNormalizingSieve();
        String mentionName  = concept.getName();
        List<String> namesKB  = concept.getNamesKnowledgeBase();
        List<String> alternateCUIs =  concept.getAlternateCuis();
        
        //System.out.println(concept.getNamesKnowledgeBase().size());
        
        //System.out.println("Normalizing sieve is " + concept.getNormalizingSieve());
        //System.out.println("Size of names KB is " + namesKB.size());
        System.out.println("Concept Name is " + concept.getName());
        for (String name : namesKB)
        {
            //Map<String, List<String>> stdTerm  = Sieve.getTerminologyNameCui();
            //Map<String, List<String>> trainTerm  = Sie
            //String cuiNameIfExists = Sieve.getTerminologyNameCui().get(name);
            String cuiNameIfExists =  Sieve.exactMatchSieve(name);
            if (!cuiNameIfExists.equals(""))
            {  
            System.out.println("Candidate is " + name);
            System.out.println("CUI is " + cuiNameIfExists);
            writer.write(concept.getName() + "\t" + concept.getCui() + "\t" + name + "\t" + cuiNameIfExists + "\n");
            }
          
        }
        
        //System.out.println(alternateCUIs.size());

    }

    private void init(String[] args) throws IOException {
        if (new File(args[0]).isDirectory())
            training_data_dir = new File(args[0]);
        else
            Util.throwIllegalDirectoryException(args[0]);
        if (new File(args[1]).isDirectory())
            test_data_dir = new File(args[1]);
        else
            Util.throwIllegalDirectoryException(args[1]);
        if (new File(args[2]).isFile())
            Terminology.terminologyFile = new File(args[2]);
        else
            Util.throwIllegalFileException(args[2]);
        
        //set stopwords, correct spellings, and abbreviations data
        boolean ncbi = test_data_dir.toString().contains("ncbi") ? true : false;
        Ling.setSpellingCorrectionMap(ncbi ? new File("resources/ncbi-spell-check.txt") : new File("resources/semeval-spell-check.txt"));
        Ling.setStopwordsList(new File("resources/stopwords.txt"));
        Abbreviation.setWikiAbbreviationExpansionMap(ncbi ? new File("resources/ncbi-wiki-abbreviations.txt") : new File("resources/semeval-wiki-abbreviations.txt"));
        Ling.setDigitToWordformMapAndReverse(new File("resources/number.txt"));
        Ling.setSuffixMap(new File("resources/suffix.txt"));
        Ling.setPrefixMap(new File("resources/prefix.txt"));
        Ling.setAffixMap(new File("resources/affix.txt"));        
        
        //initialize normalizer flags
        MultiPassSieveNormalizer.maxSieveLevel = Integer.parseInt(args[3]);
        PassAllSievesNormalizer.maxSieveLevel = Integer.parseInt(args[3]);
    }
        
    /**
     *
     * @throws IOException
     */
    public void runMultiPassSieve() throws IOException {
        Sieve.setStandardTerminology();
        Sieve.setTrainingDataTerminology();
        File f = new File("/home/megh/projects/entity-norm/cnnormaliztion/candidate.txt");
        f.createNewFile();

        System.out.println(Sieve.getStandardTerminology());
        FileWriter writer = new FileWriter(f);

        List<DocumentConcepts> dataset = Documents.getDataSet();
        List<DocumentConcepts> datasetAux = Documents.getDataSet();

        for (DocumentConcepts concepts : dataset) {
            
            Map<String, List<String>> cuiNamesMap = new HashMap<>();
            //System.out.println("Size of concepts for this iteration is " +  concepts.getConcepts().size());
            for (Concept concept : concepts.getConcepts()) {
                
                System.out.println("==========new concept===========================");
                MultiPassSieveNormalizer.applyMultiPassSieve(concept);
                
                //PassAllSievesNormalizer.applyPassAllSieves(concept);

                writeCandidateFileLine(writer, concept);
                
   
                if (concept.getCui().equals(""))
                    concept.setCui("CUI-less");
                

                
                cuiNamesMap = Util.setMap(cuiNamesMap, concept.getCui(), concept.getName());

            }

            writer.write("===========================\n");
            AmbiguityResolution.start(concepts, cuiNamesMap);
        } 
        writer.close();  
    }
    
    /**
     *
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        if (args.length == 4) {
            Main main = new Main(args);
            //Logger.setLogFile(new FileOutputStream("log.txt"));
            output_data_dir = new File(test_data_dir.toString().replace(test_data_dir.getName(), "output"));
            output_data_dir.mkdirs();            
            main.runMultiPassSieve();
            Evaluation.computeAccuracy();
            Evaluation.printResults();
        }
        else {
            System.out.println("Usage: java tool.Main <training-data-dir> <test-data-dir> <terminology/ontology-file> max-sieve-level");
            System.out.println("---------------------");
            System.out.println("Sieve levels:");
            System.out.println("1 for exact match");
            System.out.println("2 for abbreviation expansion");
            System.out.println("3 for subject<->object conversion");
            System.out.println("4 for numbers replacement");
            System.out.println("5 for hyphenation");
            System.out.println("6 for affixation");
            System.out.println("7 for disorder synonyms replacement");
            System.out.println("8 for stemming");
            System.out.println("9 for composite disorder mentions");
            System.out.println("10 for partial match");
            System.out.println("---------------------");
            System.exit(1);
        }
    }
       
}
