package com.example.dam.listacontactos;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private Adaptador ad;
    private List<Contacto> contactos;
    private ListView lv;
    boolean sem = true, orden = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        iniciar();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.desc: {
                ad.desc();
                Toast.makeText(this, R.string.label_desc, Toast.LENGTH_SHORT).show();
                return true;
            }
            case R.id.asc: {
                ad.asc();
                Toast.makeText(this, R.string.label_asc, Toast.LENGTH_SHORT).show();
                return true;
            }
            case R.id.add: {
                Intent i = new Intent(this, AddActivity.class);
                Bundle b = new Bundle();
                Contacto c = new Contacto();
                b.putSerializable("contacto", c);
                i.putExtras(b);
                startActivityForResult(i, 2);
            }
        }
        return super.onOptionsItemSelected(item);
    }

    public void iniciar() {
        lv = (ListView) findViewById(R.id.lv_contactos);
        contactos = getListaContactos(this);
        System.out.println(contactos);
        Collections.sort(contactos);
        System.out.println(contactos);
        for (Contacto c : contactos) {
            c.setNumeros(cleanRep(getListaTelefonos(this, c.getId())));
            Log.v("Contacto", c.getNombre());
        }
        ad = new Adaptador(this, R.layout.elemento_lista, (ArrayList<Contacto>) contactos);
        lv.setAdapter(ad);
        this.registerForContextMenu(lv);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                Contacto contacto = (Contacto) data.getExtras().getSerializable("contacto");
                for (Contacto c : contactos) {
                    if (c.getId() == contacto.getId()) {
                        c.setNumeros(contacto.getNumeros());
                        c.setNombre(contacto.getNombre());
                    }
                }
            }
        }
        if (requestCode == 2) {
            if (resultCode == RESULT_OK) {
                Contacto contacto = (Contacto) data.getExtras().getSerializable("contacto");
                contactos.add(contacto);
            }
        }
        ad.notifyDataSetChanged();
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo menuInfo = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        int pos = menuInfo.position;
        View v = menuInfo.targetView;
        switch (item.getItemId()) {
            case R.id.menu_editar: {
                Intent i = new Intent(this, EditActivity.class);
                Bundle b = new Bundle();
                b.putSerializable("contacto", contactos.get(pos));
                i.putExtras(b);
                startActivityForResult(i, 1);
                return true;
            }
            case R.id.menu_eliminar: {
                ad.removeContact(contactos.get(pos).getId());
                return true;
            }
            default:
                return super.onContextItemSelected(item);
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {

        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_contextual, menu);
    }

    public void mostrarNumeros(View v) {
        if (!search(v).isEmpty()) {
            TextView tv = (TextView) ((LinearLayout) ((LinearLayout) v.getParent()).getChildAt(1)).getChildAt(1);
            tv.setText("");
            if (sem) {
                for (String tlf : search(v).getNumeros()) {
                    tv.append(tlf + "\n");
                }
            } else {
                tv.append(search(v).getNumeros().get(0));
            }
            sem = !sem;
        }
    }

    private Contacto search(View v) {
        for (Contacto c : contactos) {
            if (c.getId() == (Long) v.getTag())
                return c;
        }
        return null;
    }

    private ArrayList<String> cleanRep(List<String> list) {
        ArrayList<String> clearedList = new ArrayList();
        for (String str : list) {
            if (!clearedList.contains(str))
                clearedList.add(str);
        }
        return clearedList;
    }

    public static List<Contacto> getListaContactos(Context contexto) {
        Uri uri = ContactsContract.Contacts.CONTENT_URI;
        String proyeccion[] = null;
        String seleccion = ContactsContract.Contacts.IN_VISIBLE_GROUP + " = ? and " +
                ContactsContract.Contacts.HAS_PHONE_NUMBER + "= ?";
        String argumentos[] = new String[]{"1", "1"};
        String orden = ContactsContract.Contacts.DISPLAY_NAME + " collate localized asc";
        Cursor cursor = contexto.getContentResolver().query(uri, proyeccion, seleccion, argumentos, orden);
        int indiceId = cursor.getColumnIndex(ContactsContract.Contacts._ID);
        int indiceNombre = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
        List<Contacto> lista = new ArrayList<>();
        Contacto contacto;
        while (cursor.moveToNext()) {
            contacto = new Contacto();
            contacto.setId(cursor.getLong(indiceId));
            contacto.setNombre(cursor.getString(indiceNombre));
            lista.add(contacto);
        }
        return lista;
    }

    public static List<String> getListaTelefonos(Context contexto, long id) {
        Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        String proyeccion[] = null;
        String seleccion = ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?";
        String argumentos[] = new String[]{id + ""};
        String orden = ContactsContract.CommonDataKinds.Phone.NUMBER;
        Cursor cursor = contexto.getContentResolver().query(uri, proyeccion, seleccion, argumentos, orden);
        int indiceNumero = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
        List<String> lista = new ArrayList<>();
        String numero;
        while (cursor.moveToNext()) {
            numero = cursor.getString(indiceNumero);
            lista.add(numero);
        }
        return lista;
    }

}
