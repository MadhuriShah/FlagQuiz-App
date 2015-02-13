
package com.deitel.flagquiz;

import java.io.IOException;
import java.io.InputStream;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class QuizFragment extends Fragment 
{
   private static final String TAG = "FlagQuiz Activity";
   private static final int FLAGS_IN_QUIZ = 10;   
   private List<String> fileNameList; 
   private List<String> quizCountriesList; 
   private Set<String> regionsSet; 
   private String correctAnswer; 
   private int totalGuesses; 
   private int correctAnswers; 
   private int guessRows; 
   private SecureRandom random; 
   private Handler handler; 
   private Animation shakeAnimation; 
   private TextView questionNumberTextView;
   private ImageView flagImageView; 
   private LinearLayout[] guessLinearLayouts; 
   private TextView answerTextView;  
   private Button wikiButton;
   private Button nextButton;
   private  String answer;
   private int totalFirstCorrect;
   private int score;
   private boolean isFirst;
   @Override
   
   public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState)
   {
      super.onCreateView(inflater, container, savedInstanceState);    
      View view = inflater.inflate(R.layout.fragment_quiz, container, false);
      fileNameList = new ArrayList<String>();
      quizCountriesList = new ArrayList<String>();
      random = new SecureRandom(); 
      handler = new Handler(); 
      shakeAnimation = AnimationUtils.loadAnimation(getActivity(),R.anim.incorrect_shake); 
      shakeAnimation.setRepeatCount(3); 
      questionNumberTextView =(TextView) view.findViewById(R.id.questionNumberTextView);
      flagImageView = (ImageView) view.findViewById(R.id.flagImageView);
      guessLinearLayouts = new LinearLayout[3];
      guessLinearLayouts[0] = (LinearLayout) view.findViewById(R.id.row1LinearLayout);
      guessLinearLayouts[1] =  (LinearLayout) view.findViewById(R.id.row2LinearLayout);
      guessLinearLayouts[2] = (LinearLayout) view.findViewById(R.id.row3LinearLayout);
      answerTextView = (TextView) view.findViewById(R.id.answerTextView);
      wikiButton=(Button)view.findViewById(R.id.wikiButton);
     wikiButton.setOnClickListener(wikiButtonListener); 
     nextButton=(Button)view.findViewById(R.id.nextButton);
     nextButton.setOnClickListener(nextButtonListener); 
      for (LinearLayout row : guessLinearLayouts)
      {
         for (int column = 0; column < row.getChildCount(); column++) 
         {
            Button button = (Button) row.getChildAt(column);
            button.setOnClickListener(guessButtonListener);
         }
      }  
      questionNumberTextView.setText(
         getResources().getString(R.string.question, 1, FLAGS_IN_QUIZ));
      return view;
   } 
   public void updateGuessRows(SharedPreferences sharedPreferences)
   {
      String choices=sharedPreferences.getString(MainActivity.CHOICES, null);
      guessRows = Integer.parseInt(choices) / 3;
      for (LinearLayout layout : guessLinearLayouts)
         layout.setVisibility(View.INVISIBLE);
      for (int row = 0; row < guessRows; row++) 
         guessLinearLayouts[row].setVisibility(View.VISIBLE);
   }
   public void updateRegions(SharedPreferences sharedPreferences)
   {
      regionsSet =sharedPreferences.getStringSet(MainActivity.REGIONS, null);
   }  
   public void resetQuiz() 
   {      
      AssetManager assets = getActivity().getAssets(); 
      fileNameList.clear(); 
      try 
      {
         for (String region : regionsSet) 
         {
            String[] paths = assets.list(region);

            for (String path : paths) 
               fileNameList.add(path.replace(".png", ""));
         } 
      } 
      catch (IOException exception) 
      {
         Log.e(TAG, "Error loading image file names", exception);
      } 
      
      correctAnswers = 0; 
      totalGuesses = 0; 
      quizCountriesList.clear(); 
      totalFirstCorrect = 0;
      isFirst = true;
      score = 0;
      int flagCounter = 1; 
      int numberOfFlags = fileNameList.size(); 
      while (flagCounter <= FLAGS_IN_QUIZ) 
      {
         int randomIndex = random.nextInt(numberOfFlags); 
         String fileName = fileNameList.get(randomIndex);
                  if (!quizCountriesList.contains(fileName)) 
         {
            quizCountriesList.add(fileName);
            ++flagCounter;
         } 
      } 

      loadNextFlag(); 
   } 
   private void loadNextFlag() 
   {
      String nextImage = quizCountriesList.remove(0);
      correctAnswer = nextImage; 
      answerTextView.setText(""); 
      questionNumberTextView.setText(
         getResources().getString(R.string.question, 
            (correctAnswers + 1), FLAGS_IN_QUIZ));
      String region = nextImage.substring(0, nextImage.indexOf('-'));
      AssetManager assets = getActivity().getAssets(); 

      try
      {
    	  InputStream stream = 
            assets.open(region + "/" + nextImage + ".png");
         Drawable flag = Drawable.createFromStream(stream, nextImage);
         flagImageView.setImageDrawable(flag);                       
      } 
      catch (IOException exception)  
      {
         Log.e(TAG, "Error loading " + nextImage, exception);
      } 

      Collections.shuffle(fileNameList); 
      int correct = fileNameList.indexOf(correctAnswer);
      fileNameList.add(fileNameList.remove(correct));
      for (int row = 0; row < guessRows; row++) 
      {
              for (int column = 0; 
            column < guessLinearLayouts[row].getChildCount(); column++) 
         { 
           
            Button newGuessButton = 
               (Button) guessLinearLayouts[row].getChildAt(column);
            newGuessButton.setEnabled(true);

            
            String fileName = fileNameList.get((row * 3) + column);
            newGuessButton.setText(getCountryName(fileName));
         } 
      } 
         int row = random.nextInt(guessRows); 
      int column = random.nextInt(3); 
      LinearLayout randomRow = guessLinearLayouts[row];
      String countryName = getCountryName(correctAnswer);
      ((Button) randomRow.getChildAt(column)).setText(countryName);    
   } 

     private String getCountryName(String name)
   {
      return name.substring(name.indexOf('-') + 1).replace('_', ' ');
   } 
 
   private OnClickListener guessButtonListener = new OnClickListener() 
   {
      @Override
      public void onClick(View v) 
      {
         Button guessButton = ((Button) v); 
         String guess = guessButton.getText().toString();
         answer = getCountryName(correctAnswer);
         ++totalGuesses; 
         if (guess.equals(answer)) 
         {
        	 //wikiButton.setEnabled(true);
        	 if(isFirst==true){
        		 score+=10;
        		 totalFirstCorrect++;
        	 }
        	 else{
        		 score+=5;
        	 }
            ++correctAnswers; // increment the number of correct answers
          //  url = getString(R.string.wikiURL)+Uri.encode(answer, "UTF-8");
            answerTextView.setText(answer + "!");
            answerTextView.setTextColor(
               getResources().getColor(R.color.correct_answer));

            disableButtons();
            if (correctAnswers == FLAGS_IN_QUIZ) 
            {
                             DialogFragment quizResults =  new DialogFragment()
                  {
                     // create an AlertDialog and return it
                     @Override
                     public Dialog onCreateDialog(Bundle bundle)
                     {
                        AlertDialog.Builder builder = 
                           new AlertDialog.Builder(getActivity());
                        builder.setCancelable(false); 
                        
                        builder.setMessage(
                           getResources().getString(R.string.results, 
                           totalGuesses, (1000 / (double) totalGuesses), totalFirstCorrect, score));
                        	
                        // "Reset Quiz" Button                              
                        builder.setPositiveButton(R.string.reset_quiz,
                           new DialogInterface.OnClickListener()                
                           {                                                       
                              public void onClick(DialogInterface dialog, 
                                 int id) 
                              {
                                 resetQuiz();                                      
                              } 
                           } 
                        ); 
                       
                        return builder.create();                } 
                  }; 
               
               quizResults.show(getFragmentManager(), "quiz results");
            } 
            else 
            {
            	isFirst=true;
            	wikiButton.setVisibility(View.VISIBLE);
            	nextButton.setVisibility(View.VISIBLE);
             } 
         } 
         else  
         {
        	 isFirst=false;
            flagImageView.startAnimation(shakeAnimation);
            answerTextView.setText(R.string.incorrect_answer);
            answerTextView.setTextColor(
               getResources().getColor(R.color.incorrect_answer));
            guessButton.setEnabled(false); 
         } 
      } 
   }; 
   private void disableButtons()
   {
      for (int row = 0; row < guessRows; row++)
      {
         LinearLayout guessRow = guessLinearLayouts[row];
         for (int i = 0; i < guessRow.getChildCount(); i++)
            guessRow.getChildAt(i).setEnabled(false);
      } 
   } 
   
   private OnClickListener wikiButtonListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			Intent webIntent = new Intent(Intent.ACTION_VIEW,Uri.parse("http://en.m.wikipedia.org/wiki/"+Uri.encode(answer, "UTF-8")));                      
	         startActivity(webIntent);
		}
	};
   private OnClickListener nextButtonListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			loadNextFlag();
			wikiButton.setVisibility(View.INVISIBLE);
			nextButton.setVisibility(View.INVISIBLE);
		}
	};
}
     
