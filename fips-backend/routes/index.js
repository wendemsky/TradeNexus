var express = require('express');
var router = express.Router();

/* GET home page. */
router.get('/', (req, res, next) => {
  res.render('index', { banner: 'Financial Instrument Pricing Service', title: 'FIPS', version: '1.0'});
});


module.exports = router;
