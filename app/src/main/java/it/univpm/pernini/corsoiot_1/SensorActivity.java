package it.univpm.pernini.corsoiot_1;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Point;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.model.TimeSeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;



public class SensorActivity extends AppCompatActivity implements SensorEventListener{

    private SensorManager mSensorManager;
    private Sensor mSensor;

    private Switch onOffSwitch;

    private TextView xTV;
    private TextView yTV;
    private TextView zTV;

    private TextView fxTV;
    private TextView fyTV;
    private TextView fzTV;

    private SeekBar alphaBar;

    private TextView alphaTV;

     float alpha=(float)0.8;


    float[] gravity=new float[3];
    float[] filt_acc=new float[3];

    boolean sensoractive = false;


    public static final String MyPREFERENCES = "SensorPrefs" ;
    SharedPreferences sharedpreferences;

    private XYMultipleSeriesDataset multipleDataset = new XYMultipleSeriesDataset();
    private XYMultipleSeriesRenderer multipleRenderer = new XYMultipleSeriesRenderer();
    private TimeSeries datasetX = new TimeSeries ("X");
    private TimeSeries datasetY = new TimeSeries ("Y");
    private TimeSeries datasetZ = new TimeSeries ("Z");
    private GraphicalView multipleChartView;
    private XYSeriesRenderer rendererX = new XYSeriesRenderer();
    private XYSeriesRenderer rendererY = new XYSeriesRenderer();
    private XYSeriesRenderer rendererZ = new XYSeriesRenderer();
    //	public double maxY=10;  //valori dei massimo in Y del grafico
//	public double minY=1000;
    public int timewindow=60; //larghezza finestra temporale
    private int screenW=0;  //larghezza schermo android
    private int screenH=0;  //altezza schermo android
    private int graphW=0;
    private int graphH=0;

    double time=0;

    String DEBUG="sensordebug";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);

        onOffSwitch=(Switch)findViewById(R.id.switch1);

        xTV=(TextView)findViewById(R.id.accXView);
        yTV=(TextView)findViewById(R.id.accYView);
        zTV=(TextView)findViewById(R.id.accZView);
        fxTV=(TextView)findViewById(R.id.accFiltXView);
        fyTV=(TextView)findViewById(R.id.accFiltYView);
        fzTV=(TextView)findViewById(R.id.accFiltZView);

        alphaBar=(SeekBar)findViewById(R.id.seekBar);
        alphaTV=(TextView)findViewById(R.id.barvaluetv);


        alphaBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                Log.d(DEBUG, "Alpha: "+progress);
                alpha =  (float)progress / (float)10;
                alphaTV.setText(Float.toString(alpha));
            }
        });

        //attach a listener to check for changes in state
        onOffSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {

                if(isChecked){
                    OnOffSensor(true);
                }
                else {
                    OnOffSensor(false);
                }
            }

        });

        calculateGraphDimensions();
        graphInit(graphW, graphH);
        graphSettings();

    }

    @Override
    public void onSensorChanged(SensorEvent event){
        // In this example, alpha is calculated as t / (t + dT),
        // where t is the low-pass filter's time-constant and
        // dT is the event delivery rate.
        float accx = event.values[0];
        float accy = event.values[1];
        float accz = event.values[2];

        // Isolate the force of gravity with the low-pass filter.
        gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0];
        gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1];
        gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2];

        // Remove the gravity contribution with the high-pass filter.
        filt_acc[0] = event.values[0] - gravity[0];
        filt_acc[1] = event.values[1] - gravity[1];
        filt_acc[2] = event.values[2] - gravity[2];


        xTV.setText(Float.toString(accx));
        yTV.setText(Float.toString(accy));
        zTV.setText(Float.toString(accz));

        fxTV.setText(Float.toString(filt_acc[0]));
        fyTV.setText(Float.toString(filt_acc[1]));
        fzTV.setText(Float.toString(filt_acc[2]));


        graphUpdate(filt_acc[0], filt_acc[1], filt_acc[2], time);
        time++;
    }

    @Override
    public final void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Do something here if sensor accuracy changes.
    }

    @Override
    protected void onResume() {
        super.onResume();
        getPreferences();
        if (sensoractive)
        {
            mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL);
            onOffSwitch.setChecked(true);

        }
        Log.d(DEBUG,"resume alpha: "+Float.toString(alpha));
        alphaTV.setText(Float.toString(alpha));
        int alphaint = (int)(alpha*10);
        alphaBar.setProgress(alphaint);


    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
        savePreferences();
    }

    void OnOffSensor(boolean onoff) {
        if(onoff){
            sensoractive = true;
            mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
        else {
            sensoractive = false;
            mSensorManager.unregisterListener(this);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        Log.d(DEBUG,"onSaveInstanceState");
        outState.putBoolean("sensor_active", sensoractive);
        outState.putFloat("alpha", alpha);
        outState.putDouble("time", time);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Log.d(DEBUG, "onRestoreInstanceState");
        sensoractive = savedInstanceState.getBoolean("sensor_active");
        alpha = savedInstanceState.getFloat("alpha");
        Log.d(DEBUG,"sensor:"+sensoractive+" alpha:"+Float.toString(alpha));
        alphaTV.setText(Float.toString(alpha));
        alphaBar.setProgress((int)alpha*10);
        time = savedInstanceState.getDouble("time");
    }

    void getPreferences()
    {
        alpha = sharedpreferences.getFloat("alpha", (float)0.8);
        Log.d(DEBUG,"alpha:"+Float.toString(alpha));
    }

    void savePreferences()
    {
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putFloat("alpha", alpha);
        editor.commit();
    }


    /**
     * calcola le dimensioni da dare al grafico sulla base della risoluzione dello schermo
     */
    private void calculateGraphDimensions() {

        Display display = getWindowManager().getDefaultDisplay();

        Point size = new Point();
        display.getSize(size);
        screenW = size.x;
        graphW=(int) screenW*8/10;
        screenH = size.y;
        graphH=(int) screenH*2/10;
        Log.i(DEBUG,"dimensions: W="+Integer.toString(screenW)+" , "+Integer.toString(graphW)+" H="+Integer.toString(screenH)+" , "+Integer.toString(graphH));

    }

    /**
     * Funzione per l'impostazione iniziale del grafico (dimensioni e collegamento all'elemento del layout)
     * @param w larghezza layout del grafico
     * @param h altezza layout del grafico
     */
    private void graphInit(int w, int h) {
        // TODO Auto-generated method stub
        Log.i(DEBUG,"graphsControl");

        if (multipleChartView==null){
            LinearLayout graphLayout1 = (LinearLayout) findViewById(R.id.graph);
            ViewGroup.LayoutParams params1 = graphLayout1.getLayoutParams();
            params1.width=w;
            params1.height=h;
            graphLayout1.setLayoutParams(params1);
            multipleChartView=ChartFactory.getLineChartView(this, multipleDataset, multipleRenderer);
            graphLayout1.addView(multipleChartView);
        }
        else{
            multipleChartView.repaint();
        }
    }

    /**
     * Impostazioni varie del grafico
     */
    private void graphSettings() {

        rendererX.setColor(Color.GREEN);
        rendererX.setLineWidth(1);
        rendererY.setColor(Color.RED);
        rendererY.setLineWidth(1);
        rendererZ.setColor(Color.BLUE);
        rendererZ.setLineWidth(1);

        multipleRenderer.setZoomButtonsVisible(false);
        multipleRenderer.setXTitle("");
        multipleRenderer.setYTitle("");
        multipleRenderer.setAxisTitleTextSize(20);
        multipleRenderer.setLabelsTextSize(18);
        multipleRenderer.setLegendTextSize(18);
        multipleRenderer.setLegendHeight(40);
        multipleRenderer.setApplyBackgroundColor(true);
        //multipleRenderer.setShowLegend(false);
        multipleRenderer.setLegendTextSize(22);
        //multipleRenderer.setBackgroundColor(color);
        multipleRenderer.setBackgroundColor(Color.TRANSPARENT);
        multipleRenderer.setPanEnabled(false, false);
        multipleRenderer.setMargins(new int[] {2,30,10,5});
        //multipleRenderer.setMarginsColor(color);
        multipleRenderer.setMarginsColor(Color.argb(0x00, 0x01, 0x01, 0x01));

        //multipleRenderer.setLegendHeight(-5);
        multipleRenderer.setShowGrid(false);
        multipleRenderer.addSeriesRenderer(rendererX);
        multipleRenderer.addSeriesRenderer(rendererY);
        multipleRenderer.addSeriesRenderer(rendererZ);
        multipleDataset.addSeries(datasetX);
        multipleDataset.addSeries(datasetY);
        multipleDataset.addSeries(datasetZ);
    }

    private void graphRange(int xmin, int xmax, int ymin, int ymax)
    {
        multipleRenderer.setYAxisMax(11);
        multipleRenderer.setYAxisMin(-11);
        multipleRenderer.setXAxisMax(time+timewindow);
        multipleRenderer.setXAxisMin(time);
    }

    /**
     * Aggiorna il grafico con nuovi valori
     * @param value nuovo valore
     * @param time tempo corrispondente al nuovo valore
     */
    public void graphUpdate (float valueX, float valueY, float valueZ, double time){
        Log.i(DEBUG,"graphUpdate");

        datasetX.add(time,(double)valueX);
        datasetY.add(time,(double)valueY);
        datasetZ.add(time,(double)valueZ);
        if(time<=timewindow){
            multipleRenderer.setXAxisMax(timewindow);
            multipleRenderer.setXAxisMin(0);
        }

        if (time>timewindow){
            multipleRenderer.setXAxisMax(time);
            multipleRenderer.setXAxisMin(time-timewindow);
        }
        if (multipleChartView != null)
        {
            multipleChartView.repaint();

        }
    }

    /**
     * Pulisce il grafico
     */
    private void clearGraph(){
        time=0;
        datasetX.clear();
        datasetY.clear();
        datasetZ.clear();
        multipleChartView.repaint();
    }
}
