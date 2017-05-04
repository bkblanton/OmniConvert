package convert.unitconverter;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

public class MainActivity extends AppCompatActivity {

    Editor SPE;
    Button convertButton;

    List<Unit> units; //arrayList of all the units
    Map<String, Unit> unitNames = new HashMap<>(); //map of units by name
    SortedMap<String, List<String>> unitTypes = new TreeMap<>(); //map of unit names by type
    UnitParser unitParser = new UnitParser();
    InputStream raw; //InputStream for raw xml file

    //Map<String, Integer> prefixes = new HashMap<>();

    EditText editText;
    TextView textView;
    TextView textView2;
    Spinner unitTypeSpinner;
    Spinner unit1Spinner;
    Spinner unit2Spinner;
    ArrayAdapter<String> unitTypeSpinnerAdapter;
    ArrayAdapter<String> unitSpinnerAdapter;

    String selectedUnit1;
    String selectedUnit2;
    Double result;

    Boolean updating = false;

    SharedPreferences SP;

    static double round(double num, int idp) {
        if (num == 0) {
            return num;
        }
        final double mult = Math.pow(10, idp);
        final double result = Math.floor(num * mult + 0.5)/mult;
        return result;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        convertButton = (Button) findViewById(R.id.convertButton);
        textView = (TextView) findViewById(R.id.textView);
        textView2 = (TextView) findViewById(R.id.textView2);
        editText = (EditText) findViewById(R.id.editText);
        unit1Spinner = (Spinner) findViewById(R.id.unit1Spinner);
        unit2Spinner = (Spinner) findViewById(R.id.unit2Spinner);
        unitTypeSpinner = (Spinner) findViewById(R.id.unitTypeSpinner);

        SP = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        SPE = SP.edit();

        try {
            raw = getResources().openRawResource(R.raw.units);
            units = unitParser.parse(raw); //parses units.xml and puts in units List
            raw.close();
            for (Unit unit : units) {
                unitNames.put(unit.name, unit); //put unit with name as key
                if (!unitTypes.containsKey(unit.type)) {
                    unitTypes.put(unit.type, new ArrayList<String>()); //creates new entry for List units of units by that type
                }
                unitTypes.get(unit.type).add(unit.name); //put unit in the list belonging to its type
            }

            //unit type spinner
            unitTypeSpinnerAdapter = new ArrayAdapter<>(
                    this, android.R.layout.simple_spinner_item, new ArrayList<>(unitTypes.keySet()));
            unitTypeSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            unitTypeSpinner.setAdapter(unitTypeSpinnerAdapter);
            unitTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                    updating = true;
                    result = null;
                    editText.getText().clear();
                    unitSpinnerAdapter = new ArrayAdapter<>(unitTypeSpinner.getContext(), android.R.layout.simple_spinner_item, unitTypes.get(unitTypeSpinner.getSelectedItem().toString()));
                    unitSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    unit1Spinner.setAdapter(unitSpinnerAdapter);
                    unit2Spinner.setAdapter(unitSpinnerAdapter);
                    try {
                        unit1Spinner.setSelection(unitSpinnerAdapter.getPosition(SP.getString("pref_default" + unitTypeSpinner.getSelectedItem().toString() + "1", BuildConfig.FLAVOR)));
                    } catch (Exception e) {
                    }
                    try {
                        unit2Spinner.setSelection(unitSpinnerAdapter.getPosition(SP.getString("pref_default" + unitTypeSpinner.getSelectedItem().toString() + "2", BuildConfig.FLAVOR)));
                    } catch (Exception e2) {
                    }
                    updating = false;
                    updateView();
                }

                @Override
                public void onNothingSelected(AdapterView<?> parentView) {
                    // your code here
                }
            });

            //edittext
            editText.addTextChangedListener(new TextWatcher() {
                @Override
                public void onTextChanged(CharSequence s, int start, int before,
                                          int count) {
                    updateView();
                }

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count,
                                              int after) {

                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });

            //unit spinners
            unit1Spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                    selectedUnit1 = unit1Spinner.getSelectedItem().toString();
                    SPE.putString("pref_default" + unitTypeSpinner.getSelectedItem().toString() + "1", selectedUnit1);
                    SPE.commit();
                    updateView();
                }

                @Override
                public void onNothingSelected(AdapterView<?> parentView) {
                    // your code here
                }
            });
            unit2Spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                    selectedUnit2 = unit2Spinner.getSelectedItem().toString();
                    SPE.putString("pref_default" + unitTypeSpinner.getSelectedItem().toString() + "2", selectedUnit2);
                    SPE.commit();
                    updateView();
                }

                @Override
                public void onNothingSelected(AdapterView<?> parentView) {
                    // your code here
                }
            });

        } catch (Exception e) {
            Log.d(this.getClass().getSimpleName(), e.toString());
            finish();
        }
    }

    private void updateView() {

        if (!updating) {
            updating = true;
            Unit unit1 = (Unit) unitNames.get(selectedUnit1);
            Unit unit2 = (Unit) unitNames.get(selectedUnit2);
            Unit.sigFigs = getSigFigsPreferenceValue();
            try {
                result = unit1.convert(Double.parseDouble(editText.getText().toString()), unit2);
                String resultString = Double.toString(round(result, getSigFigsPreferenceValue()));
                textView.setText(resultString);
            } catch (Exception e) {
                Log.d("EXCEPTION1", e.toString());
                textView.setText(R.string.defaultResultText);
            }
            if (getShowConversionDetailsPreferenceValue()) {
                textView2.setVisibility(View.VISIBLE);
                try { //display conversion instructions
                    if (selectedUnit1.equals(selectedUnit2)) {
                        textView2.setText("");
                    } else {
                        Double conversionRatio = unit1.conversionFactor / unit2.conversionFactor;
                        String resultString = unit2.name + " = " + unit1.name;
                        if (Math.abs(conversionRatio) > 1) {
                            resultString += (" * " + round(conversionRatio, getSigFigsPreferenceValue()));
                        } else if (Math.abs(conversionRatio) < 1) {
                            resultString += (" / " + round(unit2.conversionFactor / unit1.conversionFactor, getSigFigsPreferenceValue()));
                        }
                        if (unit1.conversionConstant - unit2.conversionConstant != 0) {
                            Double result = round((unit1.conversionConstant - unit2.conversionConstant) / unit2.conversionFactor, getSigFigsPreferenceValue());
                            if (result > 0) {
                                resultString += (" + " + result);
                            } else {
                                resultString += (" - " + Math.abs(result));
                            }
                        }

                        //resultString = !resultString.contains(".") ? resultString : resultString.replaceAll("0*$", "").replaceAll("\\.$", "");

                        textView2.setText(resultString);
                    }
                } catch (Exception ignored) {
                }
            } else {
                textView2.setVisibility(View.GONE);
            }
            updating = false;
        }
    }

    public void swapInputs(View v) {
        updating = true;

        try {
            String resultString = Double.toString(round(result, getSigFigsPreferenceValue()));
            //resultString = !resultString.contains(".") ? resultString : resultString.replaceAll("0*$", "").replaceAll("\\.$", "");
            editText.setText(resultString);
        } catch (Exception e) {
            editText.getText().clear();
        }

        int temp = unit1Spinner.getSelectedItemPosition();
        unit1Spinner.setSelection(unit2Spinner.getSelectedItemPosition());
        unit2Spinner.setSelection(temp);

        updating = false;
    }

    public void clearInput(View v) {
        editText.getText().clear();
        result = null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id) {
            case R.id.action_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                break;
            case R.id.action_about:
                // 1. Instantiate an AlertDialog.Builder with its constructor
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(R.string.about_description)
                        .setTitle(R.string.action_about);
                AlertDialog dialog = builder.create();
                dialog.show();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    public int getSigFigsPreferenceValue() {
        return Integer.parseInt(SP.getString("pref_sigFigs", "12"));
    }

    public boolean getShowConversionDetailsPreferenceValue() {
        return SP.getBoolean("pref_showConversionDetails", false);
    }

}