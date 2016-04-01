# RegexGenerator

This project contains the source code of a tool for generating regular expressions for text extraction and classification (flagging):

1. automatically,
2. based only on examples of the desired behavior,
3. without any external hint about how the target regex should look like.

An online, interactive version of this engine is accessible at: [http://regex.inginf.units.it/](http://regex.inginf.units.it/)

RegexGenerator was developed at the [Machine Learning Lab, University of Trieste, Italy] (http://machinelearning.inginf.units.it).

The provided engine is a developement release (1) that implements the algorithms published in our articles (2):

* Bartoli, Davanzo, De Lorenzo, Medvet, Sorio, Automatic Synthesis of Regular Expressions from Examples, IEEE Computer, 2014
* Bartoli, De Lorenzo, Medvet, Tarlao, Learning Text Patterns using Separate-and-Conquer Genetic Programming, 18th European Conference on Genetic Programming (EuroGP), 2015, Copenhagen (Denmark)
* Bartoli, De Lorenzo, Medvet, Tarlao, Virgolin, Evolutionary Learning of Syntax Patterns for Genic Interaction Extraction, ACM Genetic and Evolutionary Computation Conference (GECCO), 2015, Madrid (Spain)

More details about the project can be found on [Machine Learning Lab news pages](http://machinelearning.inginf.units.it/news/newregexgeneratortoolonline).

We hope that you find this code instructive and useful for your research or study activity.

If you use our code in your reasearch please cite our work and please share back your enhancements, fixes and 
modifications.

## Project Structure

The RegexGenerator project is organized in three NetBeans Java subprojects:

* ConsoleRegexTurtle:  cli frontend for the GP engine
* MaleRegexTurtle:       provides the regular expression tree representation
* Random Regex Turtle:     GP search engine 

## Other Links

Machine Learning Lab, [Twitter account](https://twitter.com/MaleLabTs)

RegexGenerator [wiki](https://github.com/MaLeLabTs/RegexGenerator/wiki) with installation walkthrough and guide

---

(1) This is a developement version branch which *slightly* differs from the cited works.

(2) BibTeX format:

    @article{bartoli2016inference, 
	  author={A. Bartoli and A. De Lorenzo and E. Medvet and F. Tarlao}, 
	  journal={IEEE Transactions on Knowledge and Data Engineering}, 
	  title={Inference of Regular Expressions for Text Extraction from Examples}, 
	  year={2016}, 
	  volume={28}, 
	  number={5}, 
	  pages={1217-1230}, 
	  doi={10.1109/TKDE.2016.2515587}, 
	  ISSN={1041-4347}, 
	  month={May},
    }
    @inproceedings{bartoli2015evolutionary,
      title={Evolutionary Learning of Syntax Patterns for Genic Interaction Extraction},
      author={Bartoli, Alberto and De Lorenzo, Andrea and Medvet, Eric and
      Tarlao, Fabiano and Virgolin, Marco},
      booktitle={Proceedings of the 2015 on Genetic and Evolutionary Computation Conference},
      pages={1183--1190},
      year={2015},
      organization={ACM}
    }

