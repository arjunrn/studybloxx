package tu.dresden.studybloxx.fragments;

import tu.dresden.studybloxx.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;


public class AddCourseDialogFragment extends DialogFragment
{
	private static final String APPOINTMENT_NAME = "appointment_name";

	public interface AddCourseListener
	{
		public void addCourse(String courseName);
	}

	AddCourseListener mAddListener;
	private String mCourseName;


	public static AddCourseDialogFragment getInstance(String appointmentName)
	{
		Bundle args = new Bundle();
		args.putString(APPOINTMENT_NAME, appointmentName);
		AddCourseDialogFragment fragment = new AddCourseDialogFragment();
		fragment.setArguments(args);
		return fragment;
	}


	@Override
	public void onAttach(Activity activity)
	{
		super.onAttach(activity);
		try
		{
			mAddListener = (AddCourseListener) activity;
		}
		catch (ClassCastException e)
		{
			throw new RuntimeException(String.format("Class %s must implement the interface %s", activity.getClass().getCanonicalName(),
				AddCourseListener.class.getCanonicalName()));
		}

		Bundle args = getArguments();
		mCourseName = args.getString(APPOINTMENT_NAME);
	}


	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState)
	{
		LayoutInflater inflater = getActivity().getLayoutInflater();

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		View dialogView = inflater.inflate(R.layout.add_course_dialog, null);
		final EditText courseInput = (EditText) dialogView.findViewById(R.id.add_course_input);
		courseInput.setText(mCourseName);
		builder.setPositiveButton(R.string.add_course_confirm, new OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				String courseName = courseInput.getText().toString();
				mAddListener.addCourse(courseName);
			}
		}).setNegativeButton(R.string.add_course_cancel, new OnClickListener()
		{

			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				dialog.cancel();
			}
		}).setTitle(R.string.add_course_title).setView(dialogView);
		return builder.create();
	}
}
