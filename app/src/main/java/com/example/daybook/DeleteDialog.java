package com.example.daybook;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

// dialog Delete do usuwania elementów z list
public class DeleteDialog extends DialogFragment {
  public interface NoticeDialogListener {
    public void onDataPositiveClick(DialogFragment dialog);

    public void onDialogNegativeClick(DialogFragment dialog);
  }

  NoticeDialogListener mListener;
  public static String object;


  public DeleteDialog() {
  }

  static DeleteDialog newInstance(String obj) {
    object = obj;
    return new DeleteDialog();
  }

  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
    builder.setTitle("Deleting " + object + "!")
      .setMessage(getResources().getString(R.string.delete_title))
      .setPositiveButton(getResources().getString(R.string.dialog_OK), new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialogInterface, int i) {
          mListener.onDataPositiveClick(DeleteDialog.this);
        }
      })
      .setNegativeButton(getResources().getString(R.string.dialog_CANCEL), new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialogInterface, int i) {
          mListener.onDialogNegativeClick(DeleteDialog.this);
        }
      });

    return builder.create();
  }

  @Override
  public void onAttach(Context context) {
    super.onAttach(context);
    Activity activity = getActivity();

    try {
      mListener = (NoticeDialogListener) activity;
    } catch (ClassCastException e) {
      throw new ClassCastException(activity.toString() + "must implement NoticeDialogListener");
    }
  }
}
