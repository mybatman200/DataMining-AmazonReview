//
//  ViewController.swift
//  Background
//
//  Created by Tri Nguyen on 6/11/18.
//  Copyright © 2018 Tri Nguyen. All rights reserved.
//

import UIKit

class ViewController: UIViewController {

    @IBOutlet weak var image1: UIImageView!
    //@IBOutlet weak var image: UIImageView!
   
    @IBOutlet weak var image2: UIImageView!
    weak var shapeLayer: CAShapeLayer?
    
    @IBOutlet weak var airplane: UIImageView!
    @IBOutlet weak var question: UIImageView!
    override func viewDidLoad() {
        super.viewDidLoad()
        roundCircle()
        questionMark()
        flyingAir()
        createQuestionMarkPath()

        // Do any additional setup after loading the view, typically from a nib.
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    override func viewDidAppear(_ animated: Bool) {
        super.viewDidAppear(animated)
        
        let orbit = CAKeyframeAnimation(keyPath: "position")
        let circlePath = UIBezierPath(arcCenter: CGPoint(x: 0,y: 0), radius:  CGFloat(0.001), startAngle: CGFloat(0), endAngle:CGFloat(Double.pi * 2), clockwise: true)
        orbit.path = circlePath.cgPath
        orbit.duration = 260
        orbit.isAdditive = true
        orbit.repeatCount = 1000000
        orbit.calculationMode = kCAAnimationPaced
        orbit.rotationMode = kCAAnimationRotateAuto
        
        image1.layer .add(orbit, forKey: "orbit")
        
        
       /* let orbit1 = CAKeyframeAnimation(keyPath: "position")
        let circlePath1 = UIBezierPath(arcCenter: CGPoint(x: 100 ,y: 100 ), radius:  CGFloat(0.1), startAngle: CGFloat(0), endAngle:CGFloat(Double.pi * 2), clockwise: false)
        orbit1.path = circlePath1.cgPath
        orbit1.duration = 180
        orbit1.isAdditive = false
        orbit1.repeatCount = 1000000
        orbit1.calculationMode = kCAAnimationPaced
        orbit1.rotationMode = kCAAnimationRotateAuto
        
        image2.layer .add(orbit1, forKey: "orbit1")*/
        
    }
    
    func roundCircle(){
        let orbit1 = CAKeyframeAnimation(keyPath: "position")
        let circlePath1 = UIBezierPath(arcCenter: CGPoint(x: 0 ,y: 0 ), radius:  CGFloat(0.001), startAngle: CGFloat(0), endAngle:CGFloat(Double.pi * 2), clockwise: false)
        orbit1.path = circlePath1.cgPath
        orbit1.duration = 180
        orbit1.isAdditive = true
        orbit1.repeatCount = 1000000
        orbit1.calculationMode = kCAAnimationPaced
        orbit1.rotationMode = kCAAnimationRotateAuto
        
        image2.layer .add(orbit1, forKey: "orbit1")
    }
    
    func questionMark(){
       /*let movement = CAKeyframeAnimation(keyPath: "position")
        let diceRoll = Int(arc4random_uniform(100) + 1)
        let diceRoll1 = Int(arc4random_uniform(100) + 1)
        let diceRoll2 = Int(arc4random_uniform(600) + 1)
        let diceRoll3 = Int(arc4random_uniform(600) + 1)

        let quesMarkPath = UIBezierPath(roundedRect: CGRect(x: diceRoll, y: diceRoll1, width: diceRoll2, height: diceRoll3), cornerRadius: CGFloat(10))
        
       // let quesMarkPath = UIBezierPath(rect: CGRect(x: 50, y: 100, width: 100, height: 100))
        movement.path = quesMarkPath.cgPath
        movement.duration = 10
        movement.isAdditive = true
        movement.repeatCount = 1000000
        movement.calculationMode = kCAAnimationPaced
        movement.rotationMode = kCAAnimationRotateAuto

        question.layer.add(movement, forKey:"movement")*/
        
    }
    
    func createQuestionMarkPath(){
        self.shapeLayer?.removeFromSuperlayer()
        
        // create whatever path you want
        
        let path = UIBezierPath()
        //path.move(to: CGPoint(x: 1000, y: 1000))
        //path.addLine(to: CGPoint(x: 1000, y: 1030))
        path.addArc(withCenter: CGPoint(x:700, y: 200), radius: CGFloat(100), startAngle: CGFloat(Double.pi), endAngle: CGFloat(Double.pi * 1/2), clockwise: true)
        //path.addLine(to: CGPoint(x: 150, y: 400))
        path.addLine(to: CGPoint(x: 670, y: 450))
        path.addLine(to: CGPoint(x: 670, y: 270))
        path.addLine(to: CGPoint(x: 700, y: 270))
        path.addArc(withCenter: CGPoint(x:700, y: 200), radius: CGFloat(70), startAngle: CGFloat(Double.pi * 1/2), endAngle: CGFloat(Double.pi), clockwise: false)
        path.addArc(withCenter: CGPoint(x:630, y: 195), radius: CGFloat(30), startAngle: CGFloat(Double.pi * 1/2), endAngle: CGFloat(Double.pi), clockwise: true)
        
        path.move(to: CGPoint(x: 700, y: 600))
        path.addArc(withCenter: CGPoint(x:680, y: 600), radius: CGFloat(20), startAngle: CGFloat(0), endAngle: CGFloat(Double.pi * 2), clockwise: true)
        
        
        // create shape layer for that path
        
        let shapeLayer = CAShapeLayer()
        shapeLayer.fillColor = #colorLiteral(red: 0, green: 0, blue: 0, alpha: 0).cgColor
        shapeLayer.strokeColor = #colorLiteral(red: 1, green: 0, blue: 0, alpha: 1).cgColor
        shapeLayer.lineWidth = 7
        shapeLayer.path = path.cgPath
        
        // animate it
        
        view.layer.addSublayer(shapeLayer)
        let animation = CABasicAnimation(keyPath: "strokeEnd")
        //animation.beginTime = CACurrentMediaTime() + 10
        animation.fromValue = 0
        animation.duration = 5
        
        animation.repeatCount = .infinity
        shapeLayer.add(animation, forKey: "MyAnimation")

        // save shape layer
        
        self.shapeLayer = shapeLayer
        
    }
    
    func flyingAir(){
        self.shapeLayer?.removeFromSuperlayer()
        
       /* let orbit1 = CAKeyframeAnimation(keyPath: "position")
        let circlePath1 = UIBezierPath(arcCenter: CGPoint(x: 0 ,y: 0 ), radius:  CGFloat(0.001), startAngle: CGFloat(0), endAngle:CGFloat(Double.pi * 2), clockwise: false)
        orbit1.path = circlePath1.cgPath
        orbit1.duration = 180
        orbit1.isAdditive = true
        orbit1.repeatCount = 1000000
        orbit1.calculationMode = kCAAnimationPaced
        orbit1.rotationMode = kCAAnimationRotateAuto
        
        image2.layer .add(orbit1, forKey: "orbit1")*/
        
        UIView.animate(withDuration: 10, delay: 0, options: [.transitionFlipFromTop], animations: {
            // this will change Y position of imageView center
            // by 1 every time you press button
            self.airplane.center.x += 500
            self.airplane.center.y -= 500

        }, completion: nil)
        
    }
    


}

