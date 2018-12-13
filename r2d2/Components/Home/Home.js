import React, { Component} from 'react';
import { StyleSheet, Text, ToolbarAndroid, Switch, View} from 'react-native'

export default class Home extends Component{
  constructor(props){
    super(props);
    state = {
      switchValue: false,
      status: 'Desconectado',

    }
  }

  render(){
    return(
      <View style = {styles.container} />
        <ToolbarAndroid title = "Batimovil" style = {styles.toolBar} />
        <View style = {styles.content} />
          <Text>Bluetooth: </Text>
          <Switch switchValue = {this.state.switchValue} />
        </View>
      </View>

    )
  }

}

const styles = StyleSheet.create({
  container:{
    backgroundColor: "#ffffff"
  },
  toolBar:{
    backgroundColor: "#000000"
  },
  content:{
    flex:1,
    justifyContent: 'center',
    alignItems: 'center'
  }
})
