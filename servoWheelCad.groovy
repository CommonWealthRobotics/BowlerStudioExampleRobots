import com.neuronrobotics.bowlerstudio.creature.ICadGenerator
import com.neuronrobotics.bowlerstudio.creature.MobileBaseCadManager
import com.neuronrobotics.bowlerstudio.physics.TransformFactory
import com.neuronrobotics.sdk.addons.kinematics.DHParameterKinematics
import com.neuronrobotics.sdk.addons.kinematics.MobileBase
import com.neuronrobotics.sdk.addons.kinematics.VitaminLocation
import com.neuronrobotics.sdk.addons.kinematics.math.TransformNR

import eu.mihosoft.vrl.v3d.CSG
import eu.mihosoft.vrl.v3d.Cube
import eu.mihosoft.vrl.v3d.Cylinder
import eu.mihosoft.vrl.v3d.Toroid
import javafx.scene.paint.Color
import javafx.scene.transform.Affine

class servoWheelGen implements ICadGenerator{
	eu.mihosoft.vrl.v3d.parametrics.CSGDatabaseInstance csgdb;
	public servoWheelGen(eu.mihosoft.vrl.v3d.parametrics.CSGDatabaseInstance db) {
		csgdb=db;
	}
	@Override
	public ArrayList<CSG> generateCad(DHParameterKinematics d, int i) {
		MobileBaseCadManager manager  = MobileBaseCadManager.get(csgdb,d.getLinkConfiguration(i));
		TransformNR offset = d.getDHStep(i).inverse();
		ArrayList<CSG> back =[]


		Affine lastLinkAffine = i==0? d.getRootListener() :d.getListener(i-1);
		Affine manipulator = d.getListener(i);
		if(manager!=null) {
			back.addAll(manager.getOriginVitamins(csgdb,
				d.getAbstractLink(i),
				manipulator,offset));
			back.addAll(manager.getDefaultVitamins(csgdb,
				d.getAbstractLink(i),
				manipulator));
			back.addAll(manager.getPreviousLinkVitamins(csgdb,
				d.getAbstractLink(i),
				lastLinkAffine));
		}

		for(CSG c:back) {
			if( c!=null) {
				c.setManufacturing({return null})
				c.setIsHole(true)
			}
		}
		
		VitaminLocation shaftData=d.getShaftVitamin(i);
		CSG shaft = manager.getVitamin(csgdb, shaftData);
		double r = d.getDH_R(i)
		double shaftGetTotalZ = shaft.getTotalZ()
		double tireRadius= 2
		CSG tire = new Toroid(r-tireRadius, r+tireRadius).toCSG().rotx(90).movez(shaftGetTotalZ/2).setColor(Color.BLACK)
		CSG wheel = new Cylinder(r,r,shaftGetTotalZ,32).toCSG()
						.difference(tire)
						.difference(shaft.transformed(TransformFactory.nrToCSG(offset.inverse())))
						.transformed(TransformFactory.nrToCSG(offset))
						.transformed(TransformFactory.nrToCSG(shaftData.getLocation()))
		wheel.setIsHole(false)
		wheel.setManipulator(d.getLinkObjectManipulator(i))
		wheel.setColor(Color.WHITE)
		back.add(wheel)
		return back;
	}

	@Override
	public ArrayList<CSG> generateBody(MobileBase b) {
		MobileBaseCadManager manager  = MobileBaseCadManager.get(csgdb,b);
		ArrayList<CSG> back =[]
		back.addAll(manager.getVitaminsDisplay(csgdb,b,b.getRootListener()));
		for(CSG c:back) {
			c.getStorage().set("no-physics",true)
		}
		back.addAll(manager.getVitamins(csgdb,b,b.getRootListener()));
		for(CSG c:back) {
			c.setManufacturing({return null})
		}
		for(DHParameterKinematics kin:b.getAllDHChains()) {
			CSG limbRoot =new Cube(1).toCSG()
			limbRoot.setManipulator(kin.getRootListener())
			back.add(limbRoot)

		}
		return back;
	}
	
	
}

return new servoWheelGen(csgdb)


