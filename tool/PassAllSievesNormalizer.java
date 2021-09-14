/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tool;

import tool.sieves.AffixationSieve;
import tool.sieves.CompoundPhraseSieve;
import tool.sieves.DiseaseModifierSynonymsSieve;
import tool.sieves.HyphenationSieve;
import tool.sieves.PartialMatchNCBISieve;
import tool.sieves.PartialMatchSieve;
import tool.sieves.PrepositionalTransformSieve;
import tool.sieves.Sieve;
import tool.sieves.SimpleNameSieve;
import tool.sieves.StemmingSieve;
import tool.sieves.SymbolReplacementSieve;
import tool.util.Concept;
import tool.util.Terminology;

/**
 *
 * @author
 */
public class PassAllSievesNormalizer {
     
    public static int maxSieveLevel;    
    
    public static boolean pass(Concept concept, int currentSieveLevel) {
        //System.out.println(concept.getCui());
        if (!concept.getCui().equals("")) {
            concept.setAlternateCuis(Sieve.getAlternateCuis(concept.getCui()));
            concept.setNormalizingSieveLevel(currentSieveLevel-1);
            System.out.println("Normalizing sieve is " + concept.getNormalizingSieve());
            //Terminology.storeNormalizedConcept(concept);
            return false;
        }
        
        if (currentSieveLevel > maxSieveLevel)
        {   
            
            return false;
        }
            
        
        return true;
    }
        
    //behavior of this class
    public static void applyPassAllSieves(Concept concept) {
        int currentSieveLevel = 1;


        // This is the sieve for exact match 
        //match with names in training data
        //Sieve 1        
        concept.setCui(Sieve.exactMatchSieve(concept.getName())); 
        if (!pass(concept, ++currentSieveLevel))
            return;
        System.out.println(concept.getNamesKnowledgeBase());

        // This is the beginning of the second type of rules 

        //Sieve 2
        concept.setCui(Sieve.exactMatchSieve(concept.getNameExpansion()));
        if (!pass(concept, ++currentSieveLevel))
  
            
        //Sieve 3
        concept.setCui(PrepositionalTransformSieve.apply(concept));
        System.out.println("Applied prepositional transforms");
        if (!pass(concept, ++currentSieveLevel))
        
        //Sieve 4
        concept.setCui(SymbolReplacementSieve.apply(concept));
        System.out.println("Applied Symbol replacement transforms");
        if (!pass(concept, ++currentSieveLevel))
        
        //Sieve 5
        concept.setCui(HyphenationSieve.apply(concept));
        System.out.println("Applied hyphenation transforms");
        if (!pass(concept, ++currentSieveLevel)) 
        
        //Sieve 6
        concept.setCui(AffixationSieve.apply(concept));
        System.out.println("Applied affixation transforms");
        if (!pass(concept, ++currentSieveLevel))
        
        //Sieve 7
        concept.setCui(DiseaseModifierSynonymsSieve.apply(concept));
        System.out.println("Applied disease modifier transforms");
        if (!pass(concept, ++currentSieveLevel))                   
       
        
        //Sieve 8
        concept.setCui(StemmingSieve.apply(concept));
        System.out.println("Applied stemming transforms");
        if (!pass(concept, ++currentSieveLevel))
                   
        
        //Sieve 9
        concept.setCui(Main.test_data_dir.toString().contains("ncbi") ? CompoundPhraseSieve.applyNCBI(concept.getName()) : CompoundPhraseSieve.apply(concept.getName()));
        if (!pass(concept, ++currentSieveLevel)) {            
            return;         
        }
                
        //Sieve 10 - this is the sieve of the partial match
        concept.setCui(SimpleNameSieve.apply(concept));
        pass(concept, ++currentSieveLevel);
        --currentSieveLevel;
        if (!concept.getCui().equals(""))
            return;                 
        //Sieve 10
        concept.setCui(Main.test_data_dir.toString().contains("ncbi") ? PartialMatchNCBISieve.apply(concept) : PartialMatchSieve.apply(concept));
        pass(concept, ++currentSieveLevel);   
        System.out.println("xxxxxx Concept not normalized xxxxxxx");     
                
    }
                    
}
