package tu.dresden.studybloxx.adapters;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import tu.dresden.studybloxx.R;


public class CourseCursorAdapter extends CursorAdapter
{

    private static final String TAG = "CourseCursorAdapter";
    private LayoutInflater mInflater;


	public CourseCursorAdapter(Context context, Cursor c, int flags)
	{
		super(context, c, flags);
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}


	@Override
	public void bindView(View arg0, Context arg1, Cursor arg2)
	{
		TextView courseTitle = (TextView) arg0.findViewById(R.id.course_title);
        Log.d(TAG, "Course Title: " + arg2.getString(1));
		courseTitle.setText(arg2.getString(1));
	}


	@Override
	public View newView(Context arg0, Cursor arg1, ViewGroup arg2)
	{
		return mInflater.inflate(R.layout.navbar_list_item, null);
	}

}
