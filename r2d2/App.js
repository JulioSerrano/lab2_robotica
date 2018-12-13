import React, { Component} from 'react';
import { StyleSheet, Text, ToolbarAndroid, View, Image} from 'react-native'
import {Appbar, Switch, Button} from  'react-native-paper'

export default class Home extends Component{

    state = {
      isBluetoothConected:false
    }
  render(){
    return(
      <View style = {styles.container} >
        <Appbar.Header style = {styles.appbar}>
          <Image source = {require('./assets/batman3.png')} style = {{width: 90, height: 90,flex:0.5}} />
          <View>
              <Text style = {{color: '#ffffff', fontSize: 22, marginTop: 10}}>BATBOTI</Text>
          </View>
        </Appbar.Header>
        <View style = {styles.content}>
          <Text style = {{color: '#c2c2c2', fontSize: 20, marginTop: 100}}>Estado Bluetooth: </Text>
          {this.state.isBluetoothConected ?
              <Text style = {{color: '#0CD22D', fontSize: 30, marginTop: 5, marginBottom: 10}}>Conectado</Text>
                :
              <Text style = {{color: '#E60E0E', fontSize: 30, marginTop: 5, marginBottom: 10}}>Desconectado</Text>
          }
            <Button mode = 'contained' style = {styles.button}>Conectar</Button>
            <Button mode = 'contained' style = {styles.button}>Control Manual</Button>
            <Button mode = 'contained' style = {styles.button}>Control Automático</Button>
        </View>

      </View>

    )
  }
}

const styles = StyleSheet.create({
  container:{
    backgroundColor: "#f2f2f2",
    flex:1
  },
  content:{
    justifyContent: 'center',
    alignItems: 'center',
    flex:0.5,
  },
  appbar:{
    top:0,
    left:0,
    right: 0,
    backgroundColor: "#000000",
    height: 150,
    borderBottomColor: "#D9E52F",
    justifyContent: 'center',
    alignItems: 'center',
    flexDirection: 'column'
  },
  button:{
   textAlign: 'center',
   borderStyle:'solid',
   borderTopLeftRadius: 30,
   borderTopRightRadius: 30,
   borderRadius: 30,
   width: '60%',
   backgroundColor: '#212121',
   marginTop:10,
   marginBottom: 10,
   height: '20%'
 },
})
