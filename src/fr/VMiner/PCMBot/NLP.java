package fr.VMiner.PCMBot;

import java.util.*; 
import edu.stanford.nlp.pipeline.*;
import edu.stanford.nlp.ling.*; 
import edu.stanford.nlp.ling.CoreAnnotations.*;  
import edu.stanford.nlp.util.CoreMap;

public class NLP
{
    public static void main(String[] args)
    {
        Properties props = new Properties(); 
        props.put("annotators", "tokenize, ssplit, pos, lemma"); 
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props, false);
        String text = "working derived processors"/* the string you want */; 
        Annotation document = pipeline.process(text);  
        String lemmaSentence = "";
        for(CoreMap sentence: document.get(SentencesAnnotation.class))
        {    
            for(CoreLabel token: sentence.get(TokensAnnotation.class))
            {       
                String word = token.get(TextAnnotation.class);      
                String lemma = token.get(LemmaAnnotation.class); 
                lemmaSentence = lemmaSentence + lemma + " ";
                System.out.println("lemmatized word :" + lemma);
            }
            
            lemmaSentence=lemmaSentence.trim();
            System.out.println("lemmatized sentence:" + lemmaSentence);
        }
    }
    
    
}