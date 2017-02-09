package geocodigos.gpsmarker.Fragments;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import geocodigos.gpsmarker.Database.DatabaseHelper;
import geocodigos.gpsmarker.Implementation.DirectoryPicker;
import geocodigos.gpsmarker.Kml.KmlLine;
import geocodigos.gpsmarker.Kml.KmlPoints;
import geocodigos.gpsmarker.Kml.KmlPolygon;
import geocodigos.gpsmarker.Models.PointModel;
import geocodigos.gpsmarker.R;
import geocodigos.gpsmarker.ViewPoint.ViewReccord;

/**
 * Created by elidioxg on 22/07/16.
 */
public class ViewPoints extends Fragment {

    private DatabaseHelper database;
    private ImageButton ibExportar, ibExcluir, ibMapa;
    private TextView tvRegistro, tvData, tvHora, tvPontos;
    public ListView listView;

    public ArrayList<PointModel> campos = new ArrayList<PointModel>();

    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.view_points, container, false);

        final View Kml = View.inflate(getActivity(), R.layout.kml_export, null);
        final EditText etCamada = (EditText) Kml.findViewById(R.id.etCamada);
        final RadioButton rbPontos = (RadioButton) Kml.findViewById(R.id.rbponto);
        final RadioButton rbLinha = (RadioButton) Kml.findViewById(R.id.rblinha);
        final RadioButton rbPoligono = (RadioButton) Kml.findViewById(R.id.rbpoligono);

        ibExportar = (ImageButton) view.findViewById(R.id.ib_exportar);
        ibExcluir = (ImageButton) view.findViewById(R.id.ib_excluir);
        ibMapa = (ImageButton) view.findViewById(R.id.ib_mapa);
        tvPontos = (TextView) view.findViewById(R.id.tv_pontos);
        listView = (ListView) view.findViewById(R.id.lv_registro);

        refreshPoints();
        ibMapa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                transaction.remove(ViewPoints.this);
                transaction.commit();
            }
        });

        ibExportar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (campos.size() > 0) {
                    //final FileOutputStream out;
                    AlertDialog.Builder alerta = new AlertDialog.Builder(getActivity());
                    alerta.setTitle(R.string.exportar_kml);
                    //alerta.setMessage("teste");
                    alerta.setCancelable(false);
                    alerta.setView(Kml);
                    alerta.setNegativeButton(R.string.nao, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ViewGroup parent = (ViewGroup) Kml.getParent();
                            parent.removeView(Kml);
                        }
                    });
                    alerta.setOnKeyListener(new DialogInterface.OnKeyListener() {
                        @Override
                        public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                            final InputMethodManager imm;
                            if (event.getAction() == KeyEvent.ACTION_DOWN ||
                                    keyCode == KeyEvent.KEYCODE_ENTER) {
                                imm = (InputMethodManager) getActivity().getSystemService(
                                        Context.INPUT_METHOD_SERVICE);
                                imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
                            }
                            if (keyCode == KeyEvent.KEYCODE_BACK) {
                                ViewGroup parent = (ViewGroup) Kml.getParent();
                                parent.removeView(Kml);
                                dialog.dismiss();
                            }
                            return false;
                        }
                    });
                    alerta.setPositiveButton(R.string.sim, new DialogInterface.OnClickListener(
                    ) {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (!etCamada.getText().toString().isEmpty()) {
                                String layerName = etCamada.getText().toString().trim();
                                String kmlText="";
                                if(database!=null) {
                                    ArrayList<PointModel> array = database.getPoints();
                                    if (rbPontos.isChecked()) {
                                        try {
                                            kmlText = (String) KmlPoints.createLayer(layerName, array);
                                        } catch (Exception e) {

                                        }
                                    }
                                    if (rbLinha.isChecked()) {
                                        try {
                                            kmlText = (String) KmlLine.createLayer(layerName, array);
                                        } catch (Exception e) {

                                        }
                                    }
                                    if (rbPoligono.isChecked()) {
                                        try {
                                            kmlText = (String) KmlPolygon.createLayer(layerName, array);
                                        } catch (Exception e) {

                                        }
                                    }
                                    try {
                                        //Open a layout with options to save the file
                                        //and also save the file
                                        Intent intent = new Intent(getActivity(), DirectoryPicker.class);
                                        intent.putExtra("nome_camada", layerName);
                                        intent.putExtra("param", kmlText);
                                        startActivity(intent);
                                        //startActivityForResult(intent, DirectoryPicker.PICK_DIRECTORY);
                                    } catch (IllegalArgumentException e) {
                                        // TODO Auto-generated catch block
                                        e.printStackTrace();
                                    } catch (IllegalStateException e) {
                                        // TODO Auto-generated catch block
                                        e.printStackTrace();
                                    }
                                }
                            } else {
                                Toast.makeText(getActivity(), R.string.no_camada,
                                        Toast.LENGTH_SHORT).show();
                            }
                            ViewGroup parent = (ViewGroup) Kml.getParent();
                            parent.removeView(Kml);

                        }
                    }).show();
                } else {
                    Toast.makeText(getActivity(), R.string.sem_pontos, Toast.LENGTH_LONG).show();
                }
            }
        });

        ibExcluir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (campos.size() > 0) {
                    AlertDialog.Builder alerta = new AlertDialog.Builder(getActivity());
                    alerta.setTitle(R.string.apagar_registros);
                    //alerta.setMessage("teste");
                    alerta.setCancelable(false);
                    //alerta.setView();
                    alerta.setNegativeButton(R.string.nao, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
                    alerta.setPositiveButton(R.string.sim, new DialogInterface.OnClickListener(
                    ) {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            database = new DatabaseHelper(getActivity());
                            database.getWritableDatabase();
                            campos.clear();
                            campos = database.getPoints();
                            ArrayList<String> al = new ArrayList<>();
                            int campos_size = campos.size();
                            for (int i = 0; i < campos_size; i++) {
                                if (Integer.parseInt(campos.get(i).getSelected()) == 1) {
                                    al.add(campos.get(i).getId());
                                }
                            }
                            database.removePoints(al);
                            database.close();
                            refreshPoints();
                            listView.setAdapter(new ListAdapter(getActivity()));
                            synchronized (listView) {
                                listView.notifyAll();
                            }
                        }
                    }).show();
                } else {
                    Toast.makeText(getActivity(), R.string.sem_pontos,
                            Toast.LENGTH_SHORT);
                }
            }
        });

        listView.setAdapter(new ListAdapter(getActivity()));

        return view;
    }

    public class ListAdapter extends BaseAdapter {

        LayoutInflater inflater;
        ViewHolder viewHolder;

        public ListAdapter(Context c) {
            inflater = LayoutInflater.from(c);
        }

        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return campos.size();
        }

        @Override
        public Object getItem(int position) {
            // TODO Auto-generated method stub
            return position;
        }

        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return position;
        }

        private class ViewHolder {
            TextView tvRegistro;
            TextView tvHora;
            TextView tvData;
            TextView tvDescricao;
            CheckBox cbSelecionado;
            ImageButton ibUp, ibDown;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            // TODO Auto-generated method stub

            if (convertView == null) {
                convertView = View.inflate(getActivity(), R.layout.list_item, null);

                viewHolder = new ViewHolder();
                viewHolder.tvRegistro = (TextView) convertView.findViewById(R.id.tvregistro);
                viewHolder.tvData = (TextView) convertView.findViewById(R.id.tvdata);
                viewHolder.tvHora = (TextView) convertView.findViewById(R.id.tvhora);
                viewHolder.tvDescricao = (TextView) convertView.findViewById(R.id.tv_descricao);
                viewHolder.cbSelecionado = (CheckBox) convertView.findViewById(R.id.cb_ponto);
                viewHolder.cbSelecionado.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        PointModel pm = new PointModel();
                        pm.setId(campos.get(position).getId());
                        database = new DatabaseHelper(getActivity());
                        database.getWritableDatabase();
                        campos.clear();
                        campos = database.getPoints();
                        if (isChecked) {
                            pm.setSelected("1");
                        } else {
                            pm.setSelected("0");
                        }
                        database.updateSelected(pm);
                        database.close();
                    }
                });
                viewHolder.ibUp = (ImageButton) convertView.findViewById(R.id.ibUp);
                viewHolder.ibUp.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        DatabaseHelper databaseHelper = new DatabaseHelper(getActivity());
                        Log.i("Position:", "Item clicked: " + String.valueOf(position));
                        databaseHelper.changeOrderUp(position);
                        refreshPoints();
                        listView.setAdapter(new ListAdapter(getActivity()));
                        synchronized (listView) {
                            listView.notifyAll();
                        }
                    }
                });
                viewHolder.ibDown = (ImageButton) convertView.findViewById(R.id.ibDown);
                viewHolder.ibDown.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        DatabaseHelper databaseHelper = new DatabaseHelper(getActivity());
                        databaseHelper.changeOrderDown(position);
                        Log.i("Position:", "Item clicked: " + String.valueOf(position));
                        refreshPoints();
                        listView.setAdapter(new ListAdapter(getActivity()));
                        synchronized (listView) {
                            listView.notifyAll();
                        }
                    }
                });

                convertView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Bundle bundle = new Bundle();
                        bundle.putInt("posicao", position);
                        bundle.putInt("total_registros", campos.size());
                        ViewReccord viewReccord = new ViewReccord();
                        Intent intent = new Intent(getActivity(), ViewReccord.class);
                        intent.putExtras(bundle);
                        startActivity(intent);

                    }
                });
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            viewHolder.tvRegistro.setText(campos.get(position).getName().trim());
            viewHolder.tvData.setText(campos.get(position).getData().trim());
            viewHolder.tvHora.setText(campos.get(position).getTime().trim());
            viewHolder.tvDescricao.setText(campos.get(position).getDescription().trim());
            if (Integer.parseInt(campos.get(position).getSelected().trim()) == 1) {
                viewHolder.cbSelecionado.setChecked(true);
            } else {
                viewHolder.cbSelecionado.setChecked(false);
            }
            return convertView;
        }
    }

    private void refreshPoints() {
        database = new DatabaseHelper(getActivity());
        database.getWritableDatabase();

        campos.clear();
        campos = database.getPoints();
        if (!campos.isEmpty()) {
            for (int i = campos.size(); i < 0; i--) {

                String id = campos.get(i).getId();
                String registro = campos.get(i).getName();
                String latitude = campos.get(i).getlatitude();
                String longitude = campos.get(i).getLongitude();
                String setor = campos.get(i).getSector();
                String norte = campos.get(i).getNorth();
                String leste = campos.get(i).getEast();
                String descricao = campos.get(i).getDescription();
                String precisao = campos.get(i).getPrecision();
                String altitude = campos.get(i).getAltitude();
                String hora = campos.get(i).getTime();
                String data = campos.get(i).getData();

                PointModel pointModel = new PointModel();

                pointModel.setId(id);
                pointModel.setName(registro);
                pointModel.setLatidude(latitude);
                pointModel.setLongitude(longitude);
                pointModel.setSector(setor);
                pointModel.setNorth(norte);
                pointModel.setEast(leste);
                pointModel.setDescription(descricao);
                pointModel.setAltitude(altitude);
                pointModel.setPrecision(precisao);
                pointModel.setTime(hora);
                pointModel.setData(data);

                campos.add(pointModel);
            }
        }
        database.close();
        tvPontos.setText(getResources().getString(R.string.num_registros)
                + "  " + Integer.toString(campos.size()));
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            refreshPoints();
            listView.setAdapter(new ListAdapter(getActivity()));
            synchronized (listView) {
                listView.notifyAll();
            }
        }
    }
}
