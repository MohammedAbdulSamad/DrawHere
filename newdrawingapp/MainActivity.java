package sam.newdrawingapp;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.os.IInterface;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TabHost;
import android.widget.Toast;
import android.Manifest;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

/**
 * Created by Mohammed Abdul Samad on 11/20/2017.
 */

public class MainActivity extends AppCompatActivity {

    private DrawingView viewDraw;
    private BackgroundView backgroundDraw;
    private PictureView chosenImage;

    private PreviewStrokeView previewPath;
    private RelativeLayout touchArea;
    private LinearLayout colorSelector;
    private LinearLayout brushSelector;

    private LinearLayout toolsMenu;
    private RelativeLayout previewBox;

    private Display appDisplay;
    private int displayY;

    //private Button currentColor;
    String currentColor;
    private ImageButton currentColorButton;

    //Color Picker Button
    private ImageButton colorPickerButton;
    private ImageButton firstColorButton;
    private boolean colorButtonPressed = false;
    private boolean isPressedHide = false;

    //Brush Picker Button
    private Button brushPickerButton;
    private boolean brushButtonPressed = false;

    //Brush Sizes
    private float small_brush;
    private float medium_brush;
    private float large_brush;

    private LinearLayout buttonLayout;
    private boolean buttonLayoutDown = false;
    private LinearLayout imageButtons;

    private ImageButton newButton;
    private ImageButton saveButton;
    private ImageButton hideButton;
    private ImageButton undoButton;
    private ImageButton addImageButton;

    private ImageButton changeBackgroundButton;

    private SeekBar slider;
    private int sliderMin = 10;
    private int sliderMax = 80;
    private int sliderStep = 5;

    private ImageButton currentToolButton;
    private boolean isBrush = true;
    private boolean isFill = false;
    private boolean isErase = false;

    private int stateToSave;

    public static final int SELECT_IMAGE = 1;
    private ImageView selectedImage;
    private boolean movingImage = false;
    private boolean imageAdded = false;
    private SeekBar imageSizeSlider;
    private int lastIMGsize = 0;


    private int startX = 0;
    private int startY = 0;
    private Matrix matrix;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        /*getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);*/

        setContentView(R.layout.activity_main);

        /********************************************************/
        Window appWindow = this.getWindow();
        appWindow.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        appWindow.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        appWindow.setStatusBarColor(/*Color.parseColor("#ff9c31")*/Color.BLACK);
        /********************************************************/

        appDisplay = getWindowManager().getDefaultDisplay();
        Point dp = new Point();
        appDisplay.getSize(dp);
        displayY = dp.y;

        viewDraw = (DrawingView)findViewById(R.id.drawing);
        backgroundDraw = (BackgroundView) findViewById(R.id.background);
        chosenImage = (PictureView) findViewById(R.id.picture);

        /*************** Memory Caching for Bitmaps ****************/
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        final int cacheSize = maxMemory / 8;
        //viewDraw.setUpMemoryCache(cacheSize);
        /************************************************************/

        previewPath = (PreviewStrokeView) findViewById(R.id.previewStrokeView);
        //previewPath.setPathPreview();
        previewBox = (RelativeLayout) findViewById(R.id.previewFrame);

        selectedImage = (ImageView) findViewById(R.id.theImage);
        imageButtons = (LinearLayout) findViewById(R.id.imageControls);

        toolsMenu = (LinearLayout) findViewById((R.id.toolsMenu));
        //toolsMenu.setVisibility(View.INVISIBLE);

        currentToolButton = (ImageButton) findViewById(R.id.brush_menu_button);

        //****************************** Slider Settings *****************************************//
        slider = (SeekBar) findViewById(R.id.seekBar);
        slider.setMax((sliderMax - sliderMin) / sliderStep);
        slider.setProgress(((sliderMax - sliderMin) / sliderStep)/2);

        slider.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        float value = sliderMin + (progress * sliderStep);
                        viewDraw.setBrushSize(value);
                        previewPath.setBrushSize(value);
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {

                    }
                }
        );
        /******************************************************************************************/

        colorSelector = (LinearLayout)findViewById(R.id.colorTab);
        colorSelector.setVisibility(View.INVISIBLE);
        //brushSelector = (LinearLayout)findViewById(R.id.brushTab);
        //brushSelector.setVisibility(View.INVISIBLE);

        LinearLayout paintLayout = (LinearLayout)findViewById(R.id.colorPaint);

        currentColor = "#000000"; // Default color (Black)
        viewDraw.setColor(currentColor); //Sets default color
        //viewDraw.setBackgroundColor(Color.WHITE);

        //previewPath.setPathPreview();

        buttonLayout = (LinearLayout) findViewById(R.id.buttonLayout);

        colorPickerButton = (ImageButton) findViewById(R.id.colorButton);
        firstColorButton = (ImageButton) findViewById(R.id.firstColorButton);
        currentColorButton = firstColorButton;

        newButton = (ImageButton) findViewById(R.id.newButton);
        saveButton = (ImageButton) findViewById(R.id.saveButton);
        hideButton = (ImageButton) findViewById(R.id.hideButton);
        undoButton = (ImageButton) findViewById(R.id.undoButton);
        addImageButton = (ImageButton) findViewById(R.id.addImageButton);

        changeBackgroundButton = (ImageButton) findViewById(R.id.backgroundButton);

        //Checks touch input and hides tools button//
        viewDraw.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction()==MotionEvent.ACTION_DOWN) {
                    if (!movingImage) {
                        if (!colorButtonPressed) {
                            if (!isPressedHide) {
                               if (event.getY() > ((displayY/2)+(displayY/8))) {
                                   buttonLayoutDown = true;
                                   Animation animate = new TranslateAnimation(0, 0, 0, 300);
                                   animate.setDuration(500);
                                   animate.setFillAfter(true);
                                   buttonLayout.startAnimation(animate);
                                   buttonLayout.setVisibility(View.INVISIBLE);
                               }
                            }
                            if (!movingImage) {
                                viewDraw.setTouch(true);
                            }
                        }
                        brushButtonPressed = false;
                    }
                }

                if (event.getAction()==MotionEvent.ACTION_MOVE) {
                    if (!movingImage && !colorButtonPressed && !isPressedHide && !buttonLayoutDown) {
                        if (event.getY() > ((displayY/2)+(displayY/8))) {
                            buttonLayoutDown = true;
                            Animation animate = new TranslateAnimation(0, 0, 0, 300);
                            animate.setDuration(500);
                            animate.setFillAfter(true);
                            buttonLayout.startAnimation(animate);
                            buttonLayout.setVisibility(View.INVISIBLE);
                        }
                    }
                }

                if (event.getAction()==MotionEvent.ACTION_UP) {
                    if (!movingImage) {
                        if (!colorButtonPressed && !isPressedHide && buttonLayoutDown) {
                            Animation animate = new TranslateAnimation(0, 0, 300, 0);
                            animate.setDuration(500);
                            animate.setFillAfter(true);
                            buttonLayout.startAnimation(animate);
                            buttonLayout.setVisibility(View.VISIBLE);
                            buttonLayoutDown = false;
                        }
                        colorButtonPressed = false;
                    }
                }

                return false;
            }
        });


        //Brush Sizes
        small_brush = getResources().getInteger(R.integer.small_size);
        medium_brush = getResources().getInteger(R.integer.medium_size);
        large_brush = getResources().getInteger(R.integer.large_size);

    }

    /***********************************************************************************************
    *
    * Adding Images to the Drawing
    *
    ***********************************************************************************************/
    public void moveImage() {
        movingImage = true;

        imageButtons.setAlpha(0.0f);
        imageButtons.animate().alpha(1.0f).setListener(null);
        imageButtons.setVisibility(View.VISIBLE);
        //buttonLayout.setVisibility(View.GONE);

        viewDraw.setTouch(false);
        //viewDraw.allowImageMove(true);
        chosenImage.allowImageMove(true);
        //viewDraw.setVisibility(View.INVISIBLE);
        viewDraw.setAlpha(0.5f);
        Toast tt = Toast.makeText(getBaseContext(), "Touch Disabled ", Toast.LENGTH_LONG);
        tt.show();
    }

    public void rotateImage(View view) {
        chosenImage.rotateImage();
    }

    public void moveImageFinish(View view) {
        movingImage = false;

        imageButtons.setAlpha(1.0f);
        imageButtons.animate().alpha(0.0f).setListener(null);
        imageButtons.setVisibility(View.GONE);
        buttonLayout.setVisibility(View.VISIBLE);

        viewDraw.setTouch(true);
        //viewDraw.allowImageMove(false);
        chosenImage.allowImageMove(false);
        //viewDraw.setVisibility(View.VISIBLE);
        viewDraw.setAlpha(1.0f);
        setDefaultBrush();
        Toast tt = Toast.makeText(getBaseContext(), "Drawing Enabled", Toast.LENGTH_LONG);
        tt.show();
    }

    public void removeImage(View view) {
        imageAdded = false;
        chosenImage.removeImage();
        moveImageFinish(view);
    }

    public void addImage(View view) {
        if (imageAdded) {
            moveImage();
        } else {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");

            Intent pickIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            pickIntent.setType("image/*");

            Intent chooseIntent = Intent.createChooser(intent, "Select Image");
            chooseIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{pickIntent});

            startActivityForResult(chooseIntent, SELECT_IMAGE);
        }
    }

    InputStream ss;
    private int[] imgViewLocation;
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SELECT_IMAGE && data != null) {
            Uri imageChosen = data.getData();
            String[] filePath = { MediaStore.Images.Media.DATA };

            Cursor cursor = getContentResolver().query(imageChosen, filePath, null, null, null);
            cursor.moveToFirst();

            String imagePath = cursor.getString(cursor.getColumnIndex(filePath[0]));

            try {
                InputStream file = getContentResolver().openInputStream(imageChosen);
                Drawable imgD = Drawable.createFromStream(file, imageChosen.toString());
                chosenImage.setImage(imgD);
                moveImage();
                imageAdded = true;
            } catch (Exception e) {

            } finally {
                try {
                    if (ss != null) {
                        ss.close();
                    }
                } catch (Exception e) {

                }
            }
            cursor.close();
        }
    }
    /***********************************************************************************************
     **********************************************************************************************/

    int undoCount = 0;
    public void undo(View view) {
        viewDraw.undo();
    }

    /******************************** OPTIONS MENU ************************************************/
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.nav_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //Handling selecting item from top right menu
        switch (item.getItemId()) {
            case R.id.newB:
                //restartPage();
                break;
            case R.id.saveB:
                verifyStoragePermissions(this);
                break;
            case R.id.hideButton:
                //hideButton();
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    //Restarts the page for a new blank page
    public void restartPage(View view) {
        Cache.getInstance().getLru().evictAll();

        Intent i = getBaseContext().getPackageManager().getLaunchIntentForPackage(getBaseContext().getPackageName());
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        //i.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
        startActivity(i);
        System.exit(0);
    }
    /**********************************************************************************************/

    /********************** Hides the buttons for extra viewing *******************************/
    public void hideButtons(View view) {
        if (isPressedHide) {
            colorPickerButton.setVisibility(View.VISIBLE);
            //brushPickerButton.setVisibility(View.VISIBLE);
            newButton.setVisibility(View.VISIBLE);
            saveButton.setVisibility(View.VISIBLE);
            undoButton.setVisibility(View.VISIBLE);
            addImageButton.setVisibility(View.VISIBLE);
            hideButton.setImageDrawable(getResources().getDrawable(R.drawable.hidev2));
            hideButton.setAlpha(1.0f);
            isPressedHide = false;
        } else {
            colorPickerButton.setVisibility(View.INVISIBLE);
            //brushPickerButton.setVisibility(View.INVISIBLE);
            newButton.setVisibility(View.INVISIBLE);
            saveButton.setVisibility(View.INVISIBLE);
            undoButton.setVisibility(View.INVISIBLE);
            addImageButton.setVisibility(View.INVISIBLE);
            hideButton.setAlpha(0.5f);
            hideButton.setImageDrawable(getResources().getDrawable(R.drawable.hide2));
            colorSelector.setVisibility(View.INVISIBLE);
            colorPickerButton.clearColorFilter();
            toolsMenu.setVisibility(View.INVISIBLE);
            colorButtonPressed = false;
            brushButtonPressed = false;
            isPressedHide = true;
        }
    }
    /**********************************************************************************************/

    /*********************************** Starts Saving Process ************************************/
    public void startSave(View view) {
        verifyStoragePermissions(this);
    }
    /**********************************************************************************************/

    /*********************** PERMISSION REQUEST FOR SAVING TO MEMORY ******************************/
    public void onRequestPermission(int requestCode, String[] permissions, int[] grantedResults) {
        switch (requestCode) {
            case REQUEST_EXTERNAL_STORAGE: {
                if (grantedResults.length > 0 && grantedResults[0] == PackageManager.PERMISSION_GRANTED) {
                    saveNewDrawing();
                } else {

                }
                return;
            }
        }
    }
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    public  void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        } else {
            saveNewDrawing();
        }
    }
    /**********************************************************************************************/

    /*************************************** Saves Drawing ****************************************/
    public void saveNewDrawing() {
        String filename = new SimpleDateFormat("ddMMyyyy-HHmmss").format(new Date());
        Bitmap b = Bitmap.createBitmap(viewDraw.getWidth(),viewDraw.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);
        backgroundDraw.draw(c);
        selectedImage.draw(c);
        viewDraw.draw(c);
        if (isExternalStorageWritable()) {
            File root = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getPath(), "DrawHerePictures");
            if (!root.exists()) {
                root.mkdirs();
            }
            File theDrawing = new File(root, filename+".jpg");
            try {
                FileOutputStream fout = new FileOutputStream(theDrawing);
                b.compress(Bitmap.CompressFormat.JPEG, 100, fout);
                fout.flush();
                fout.close();
                Toast savedToast = Toast.makeText(getApplicationContext(), "Image saved.", Toast.LENGTH_SHORT);
                savedToast.show();
                galleryAddPic(theDrawing.getPath());
            } catch (Exception e) {
                e.printStackTrace();
                Toast savedToast = Toast.makeText(getApplicationContext(),e.toString() , Toast.LENGTH_LONG);
                savedToast.show();
            }
        } else {
            Toast savedToast = Toast.makeText(getApplicationContext(), "Not.", Toast.LENGTH_SHORT);
            savedToast.show();
        }
    }
    /**********************************************************************************************/

    /************************ Adds Saved Drawing to Gallery ***************************************/
    private void galleryAddPic(String path) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(path);//your file path
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }
    /**********************************************************************************************/

    /**************** Checks if external storage is available for read and write ******************/
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    /* Checks if external storage is available to at least read */
    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }
    /**********************************************************************************************/

    /*public File getAlbumStorageDir(String albumName) {
        // Get the directory for the user's public pictures directory.
        File file = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), albumName);
        if (!file.mkdirs()) {
            Log.e(LOG_TAG, "Directory not created");
        }
        return file;
    }*/


    /********************** Hides the tool button for extra viewing *******************************/
    /*public void hideButton() {
        if (isPressedHide) {
            colorPickerButton.setVisibility(View.VISIBLE);
            //brushPickerButton.setVisibility(View.VISIBLE);
            isPressedHide = false;
        } else {
            colorPickerButton.setVisibility(View.INVISIBLE);
            //brushPickerButton.setVisibility(View.INVISIBLE);
            colorSelector.setVisibility(View.INVISIBLE);
            toolsMenu.setVisibility(View.INVISIBLE);
            brushSelector.setVisibility(View.INVISIBLE);
            colorButtonPressed = false;
            brushButtonPressed = false;
            isPressedHide = true;


        }
    }*/
    /**********************************************************************************************/

    /********************* Sets the paint color to the color chosen *******************************/
    public void paintChosen(View view) {
        ImageButton theButton = (ImageButton)view;
        String color = view.getTag().toString();
        if (!(color.equals(currentColor))) {
            if (currentColorButton!=null) {
                currentColorButton.setImageDrawable(getResources().getDrawable(R.drawable.color_select));
            }
            theButton.setImageDrawable(getResources().getDrawable(R.drawable.color_select_selected));
            currentColorButton = theButton;
            viewDraw.setColor(color);
            colorPickerButton.setBackgroundColor(Color.parseColor(color));
            changeBackgroundButton.setBackgroundColor(Color.parseColor(color));
            previewPath.setBrushColor(color);
            currentColor = color;
            //viewDraw.setErase(false);
        }
    }
    /**********************************************************************************************/

    /**********************************************************************************************/

    /********************* Displays the color selector ********************************************/
    public void showColors(View view) {
        if (colorButtonPressed) {
            //colorSelector.setAlpha(1.0f);
            //colorSelector.animate().alpha(0.0f).setListener(null);

            //colorSelector.setVisibility(View.INVISIBLE);
            colorPickerButton.clearColorFilter();

            toolsMenu.setAlpha(1.0f);
            toolsMenu.animate().alpha(0.0f).setListener(null);
            toolsMenu.setVisibility(View.INVISIBLE);

            colorButtonPressed = false;

            viewDraw.setTouch(true);
        } else {
            //colorSelector.setVisibility(View.VISIBLE);
            colorButtonPressed = true;
            colorPickerButton.setColorFilter(Color.parseColor("#70cccccc"));
            previewPath.setPathPreview();

            toolsMenu.setVisibility(View.VISIBLE);
            toolsMenu.setAlpha(0.0f);
            toolsMenu.animate().alpha(1.0f).setListener(null);

            viewDraw.setTouch(false);
        }
    }
    /**********************************************************************************************/

    /************************** Set Brush Size ****************************************************/
    /**********************************************************************************************/

    /*************************** Tools Menu *******************************************************/
    public void selectTool(View view) {
        ImageButton selectedButton = (ImageButton) view;
        switch (selectedButton.getId()) {
            case R.id.fill_menu_button:
                startFill();
                currentToolButton.setBackground(null);
                //selectedButton.setBackground(getResources().getDrawable(R.drawable.chosen_button));
                selectedButton.setBackgroundColor(getResources().getColor(R.color.mainColor));
                currentToolButton = selectedButton;
                colorPickerButton.setImageDrawable(getResources().getDrawable(R.drawable.fill));
                break;
            case R.id.brush_menu_button:
                setDefaultBrush();
                currentToolButton.setBackground(null);
                //selectedButton.setBackground(getResources().getDrawable(R.drawable.chosen_button));
                selectedButton.setBackgroundColor(getResources().getColor(R.color.mainColor));
                currentToolButton = selectedButton;
                colorPickerButton.setImageDrawable(getResources().getDrawable(R.drawable.updated_brushv2));
                break;
            case R.id.eraserButton:
                beginErase();
                currentToolButton.setBackground(null);
                //selectedButton.setBackground(getResources().getDrawable(R.drawable.chosen_button));
                selectedButton.setBackgroundColor(getResources().getColor(R.color.mainColor));
                currentToolButton = selectedButton;
                colorPickerButton.setImageDrawable(getResources().getDrawable(R.drawable.eraser));
                break;
        }
    }
    /**********************************************************************************************/


    /************************ Enable Erasing ******************************************************/
    public void beginErase() {
        isBrush = false;
        isFill = false;
        isErase = true;
        viewDraw.setErase(true);
        viewDraw.setFill(false);
    }
    /**********************************************************************************************/

    /*********************** Sets Background With Paint Selected **********************************/
    public void setBackgroundColor(View view) {
        //viewDraw.setBackgroundCol(currentColor);
        backgroundDraw.setBackgroundCol(currentColor);
        previewBox.setBackgroundColor(Color.parseColor(currentColor));
    }
    /**********************************************************************************************/

    /************************ Start Filling *******************************************************/
    public void startFill() {
        isBrush = false;
        isErase = false;
        isFill = true;

        viewDraw.setFill(true);
    }
    /**********************************************************************************************/

    /*********************** Set Brush to Default *************************************************/
    public void setDefaultBrush() {
        isFill = false;
        isErase = false;
        isBrush = true;

        viewDraw.setErase(false);
        viewDraw.setFill(false);
        viewDraw.setPenStyle(false);
        //viewDraw.setTouch(true);
    }
    /**********************************************************************************************/

    public void setPenBrush(View view) {
        // TO DO
    }
}
