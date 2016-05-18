package com.granzotto.mqttpainel.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.granzotto.mqttpainel.R
import com.granzotto.mqttpainel.activities.AddEquipmentActivity
import com.granzotto.mqttpainel.adapters.EquipmentCardAdapter
import com.granzotto.mqttpainel.adapters.EquipmentStateListener
import com.granzotto.mqttpainel.models.EquipmentObj
import com.granzotto.mqttpainel.presenters.EquipmentsCardPresenter
import com.granzotto.mqttpainel.utils.ConnectionManager
import com.granzotto.mqttpainel.utils.MessageReceivedListener
import io.realm.RealmResults
import kotlinx.android.synthetic.main.fragment_equipments.*
import nucleus.factory.RequiresPresenter
import nucleus.view.NucleusFragment
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.jetbrains.anko.startActivity

/**
 * Created by marciogranzotto on 5/17/16.
 */

@RequiresPresenter(EquipmentsCardPresenter::class)
class EquipmentsFragment : NucleusFragment<EquipmentsCardPresenter>(), MessageReceivedListener, EquipmentStateListener {

    companion object {
        val TAG = "EquipmentsFragment"
    }

    var adapter: EquipmentCardAdapter? = null

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.fragment_equipments, container, false)
    }

    override fun onStart() {
        super.onStart()

        addButton.setOnClickListener { addButtonClicked() }

        ConnectionManager.addRecievedListener(this, TAG)

        presenter.requestEquipments()
    }

    override fun onStop() {
        ConnectionManager.removeRecievedListener(TAG)
        super.onStop()
    }

    private fun addButtonClicked() {
        startActivity<AddEquipmentActivity>()
    }

    override fun messageReceived(topic: String?, message: MqttMessage?) {
        if (topic != null) {
            presenter.messageRecieved(topic, message)
        }
    }

    fun reloadEquipments(response: RealmResults<EquipmentObj>) {
        adapter?.items = response
        adapter?.notifyDataSetChanged()
    }

    fun onEquipmentsSuccess(response: RealmResults<EquipmentObj>) {
        adapter = EquipmentCardAdapter(response, this)
        recyclerView.adapter = adapter

        response.forEach {
            ConnectionManager.client?.subscribe(it.topic, 0)
        }
    }

    override fun stateChanged(equipment: EquipmentObj, state: Boolean) {
        presenter.stateChanged(equipment, state)
    }
}
