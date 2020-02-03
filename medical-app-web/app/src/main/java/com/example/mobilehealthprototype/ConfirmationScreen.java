package com.example.mobilehealthprototype;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.ajithvgiri.searchdialog.SearchListItem;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

public class ConfirmationScreen extends AppCompatActivity {
    Intent passedIntent;
    Sex p_sex;
    int p_id, p_age;
    float p_height, p_weight;

    int diagnosed_disease_index;
    String diagnosed_disease;
    Float diagnosed_disease_prob;

    List<SearchListItem> allDiseases = new ArrayList<>();
    ArrayList<String> patientSymptoms;

    Hashtable<String, String> SympToUmls= new Hashtable<String, String>();
    Hashtable<String, String> UmlsToSymp= new Hashtable<String, String>();
    Hashtable<Integer, String> IndexToUmls_s = new Hashtable<Integer, String>();
    Hashtable<String, Integer> UmlsToIndex_s = new Hashtable<String, Integer>();
    Hashtable<String, Integer> UmlsToIndex_d = new Hashtable<String, Integer>();
    Hashtable<Integer, String> IndexToUmls_d = new Hashtable<Integer, String>();
    Hashtable<String, String> DisToUmls = new Hashtable<String, String>();
    Hashtable<String,String> UmlsToDis = new Hashtable<String,String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirmation_screen);

        handlePassedIntent();
        String summary = constructConfirmationDetails();
        TextView text = findViewById(R.id.textView5);
        text.setText(summary);

        Button confirmation_button = (Button) findViewById(R.id.final_confirmation);
        confirmation_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.out.println(p_height);
                CommunicationHandler ch = new CommunicationHandler();
                System.out.println(p_weight);
                ArrayList<Integer> tmp = new ArrayList<Integer>();
                System.out.println(patientSymptoms.size());
                for(int i = 0; i < patientSymptoms.size(); i++){
                    tmp.add(UmlsToIndex_s.get(SympToUmls.get(patientSymptoms.get(i))));
                    //new Integer(UmlsToIndex.get(SympToUmls.get(patientSymptoms.get(i))))
                }
                System.out.println(p_age);

                String toSend = ch.generateRawMessage(p_id, p_sex, p_age, p_height, p_weight, tmp, diagnosed_disease_index);
                Log.d("TESTING", "IS THIS WORKING?"); //TODO: DELETE
                ch.sendEncryptedMessage(getApplicationContext(), getString(R.string.server_number),toSend); //Check if this is working later
                saveFile(toSend);
                readFile();

                Intent intent = new Intent(ConfirmationScreen.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }

    private void readFile() {
        try {
            FileInputStream fileInputStream = openFileInput("cache_text");
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fileInputStream));
            String line = bufferedReader.readLine();
            while (line != null) {
                System.out.println("file contents:");
                System.out.println(line);
                line = bufferedReader.readLine();
            }
            bufferedReader.close();
            fileInputStream.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveFile(String toSend) {
        try {
            FileOutputStream fileOutputStream = openFileOutput("cache_text", MODE_APPEND);
            PrintWriter writer = new PrintWriter(new OutputStreamWriter(fileOutputStream));
            writer.write(toSend+"\n");
            writer.flush();
            writer.close();
            fileOutputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    public String constructConfirmationDetails(){
        String spid = Integer.toString(p_id);
        String spage = Integer.toString(p_age);
        String spsex = (p_sex == Sex.MALE) ? "M" : "F";
        String spheight = Float.toString(p_height);
        String spweight = Float.toString(p_weight);

        String sddprob = Float.toString(diagnosed_disease_prob);

        String fp = "Patient id:" + spid + "\n" + "Patient age:" + spage + "\n";
        String sp = fp + "Patient Symptoms::" + "\n";
        for(int i = 0; i < patientSymptoms.size(); i++){
            sp = sp + patientSymptoms.get(i) + "\n";
        }
        String tp = sp + "Doctor Diagnosis::" + "\n" + diagnosed_disease + "\n" + "with probability: " + sddprob;
        return tp;
    }

    public void handlePassedIntent(){
        passedIntent = getIntent();
        p_sex = (Sex) passedIntent.getSerializableExtra("sex");
        p_id = passedIntent.getIntExtra("hid", -1);
        p_age = passedIntent.getIntExtra("age", -1);
        p_height = passedIntent.getFloatExtra("height",-1);
        p_weight = passedIntent.getFloatExtra("weight",-1);
        patientSymptoms = passedIntent.getStringArrayListExtra("patient_symptoms");

        SympToUmls = new Hashtable<> ((HashMap<String,String>) passedIntent.getSerializableExtra("stu"));
        UmlsToSymp = new Hashtable<>((HashMap<String,String>) passedIntent.getSerializableExtra("uts"));
        IndexToUmls_s = new Hashtable<>((HashMap<Integer, String>) passedIntent.getSerializableExtra("itus"));
        UmlsToIndex_s = new Hashtable<>((HashMap<String, Integer>) passedIntent.getSerializableExtra("utis"));

        DisToUmls = new Hashtable<> ((HashMap<String,String>) passedIntent.getSerializableExtra("dtu"));
        UmlsToDis = new Hashtable<>((HashMap<String,String>) passedIntent.getSerializableExtra("utd"));
        IndexToUmls_d = new Hashtable<>((HashMap<Integer, String>) passedIntent.getSerializableExtra("itud"));
        UmlsToIndex_d = new Hashtable<>((HashMap<String, Integer>) passedIntent.getSerializableExtra("utid"));

        diagnosed_disease = passedIntent.getStringExtra("diagnosed_disease_name");
        diagnosed_disease_prob = passedIntent.getFloatExtra("likelihood_of_disease", -1f);
        diagnosed_disease_index = passedIntent.getIntExtra("diagnosed_disease_index", -1);
    }
}
