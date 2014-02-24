package tu.dresden.studybloxx.adapters;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import tu.dresden.studybloxx.R;


public class NotesCursorAdapter extends CursorAdapter
{

	private LayoutInflater mInflater;


	public NotesCursorAdapter(Context context, Cursor c, int flags)
	{
		super(context, c, flags);
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}


	@Override
	public void bindView(View view, Context arg1, Cursor cur)
	{
		String title = cur.getString(1);
		String url = cur.getString(2);
		TextView titleView = (TextView) view.findViewById(android.R.id.title);
		titleView.setText(title);
	}


	@Override
	public View newView(Context arg0, Cursor arg1, ViewGroup arg2)
	{
		return mInflater.inflate(R.layout.notes_list_item, null);
	}

}
