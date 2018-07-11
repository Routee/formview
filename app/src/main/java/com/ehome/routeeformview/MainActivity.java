package com.ehome.routeeformview;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.ehome.formview.RouteeFormView;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, RouteeFormView.DataChangeListener {

    private EditText       mEtX;
    private EditText       mEtY1;
    private EditText       mEtY2;
    private Button         mBtDelete;
    private Button         mBtAdd;
    private RouteeFormView mRfv;
    private ArrayList<RouteeFormView.Units> mUnits1 = new ArrayList();
    private ArrayList<RouteeFormView.Units> mUnits2 = new ArrayList();
    private Button  mBtShader;
    private boolean mShaderable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mEtX = findViewById(R.id.et_x);
        mEtY1 = findViewById(R.id.et_y1);
        mEtY2 = findViewById(R.id.et_y2);
        mBtDelete = findViewById(R.id.bt_delete);
        mBtShader = findViewById(R.id.bt_shader);
        mBtAdd = findViewById(R.id.bt_add);
        mRfv = findViewById(R.id.rfv);
        mBtAdd.setOnClickListener(this);
        mBtShader.setOnClickListener(this);
        mBtDelete.setOnClickListener(this);
        mUnits1.add(0, new RouteeFormView.Units(0, "0"));
        mUnits2.add(0, new RouteeFormView.Units(0, "0"));
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.bt_add:
                addNewData();
                break;
            case R.id.bt_shader:
                mRfv.setShaderable(mShaderable);
                mShaderable = !mShaderable;
                break;
            case R.id.bt_delete:
                removeLastData();
                break;
            default:
                break;
        }
    }

    private void removeLastData() {
        if (mUnits1.size() <= 2) {
            return;
        }
        Map<Integer, List<RouteeFormView.Units>> map = new LinkedHashMap<>();
        mUnits1.remove(mUnits1.size() - 1);
        mUnits2.remove(mUnits2.size() - 1);
        map.put(Color.parseColor("#FFB3B3"), mUnits1);
        map.put(Color.parseColor("#FAFA44"), mUnits2);
        mRfv.resetData(map);
        mRfv.setOnHelpDataChangedListener(this);
    }

    private void addNewData() {
        String x = mEtX.getText().toString();
        String y1 = mEtY1.getText().toString();
        String y2 = mEtY2.getText().toString();
        if (x.isEmpty() || y1.isEmpty() || y2.isEmpty()) {
            return;
        }
        double y1Value = 0;
        double y2Value = 0;
        try {
            y1Value = Double.parseDouble(y1);
            y2Value = Double.parseDouble(y2);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        Map<Integer, List<RouteeFormView.Units>> map = new LinkedHashMap<>();
        mUnits1.add(new RouteeFormView.Units(y1Value, x));
        mUnits2.add(new RouteeFormView.Units(y2Value, x));
        map.put(Color.parseColor("#FFB3B3"), mUnits1);
        map.put(Color.parseColor("#FAFA44"), mUnits2);
        mRfv.resetData(map);
        mRfv.setOnHelpDataChangedListener(this);
    }

    @Override
    public void dataChanged(int color, int position) {
        position = position - 1;
        List<List<RouteeFormView.TextUnit>> textUnits = new ArrayList<>();
        List<RouteeFormView.TextUnit> line1 = new ArrayList<>();
        line1.add(new RouteeFormView.TextUnit(color, "line1 = " + mUnits1.get(position + 1).y));
        textUnits.add(line1);

        List<RouteeFormView.TextUnit> line2 = new ArrayList<>();
        line2.add(new RouteeFormView.TextUnit(color, "line2 = " + mUnits2.get(position + 1).y));
        textUnits.add(line2);
        mRfv.setHelpText(textUnits);
    }
}
